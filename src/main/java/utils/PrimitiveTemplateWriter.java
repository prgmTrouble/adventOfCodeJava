package utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for generating Java code parameterized by primitive numeric types.
 *
 * <h4>Variables</h4>
 * <p>Template variables can contain '{@code i}' for integral types (e.g. {@code byte}, {@code int}, etc.)
 * and/or '{@code f}' for floating point types. Variables are expected in a comma-separated list terminated
 * by a semi-colon (e.g. {@code i,f,if;})</p>
 *
 * <h4>Segments</h4>
 * <p>Segments begin with the first non-whitespace or escaped character following the variable list and end with
 * the character '{@code #}' or EOF, whichever appears first. Variables are referenced in the following form:
 * {@code $[0-9]+(\+|\-)}. The number indicates which variable to use and the '{@code +}' or '{@code -}' determines
 * whether the first character is uppercase or lowercase, respectfully. For example, the template
 * {@code i,f; $1+ or $0-} will produce 8 outputs, the first of which being '{@code Float or byte}'. Any character
 * (especially whitespace and '{@code $}') be escaped using the '{@code \}' character.</p>
 *
 * <h4>Static Templates</h4>
 * <p>Static templates are a sequence of alternating variable lists and sequences. The output file will share the
 * name of the template file. For example:
 * <pre>
 *     f;
 *     foo=$0-
 *     #i;
 *     bar=$0-
 * </pre>
 * will generate
 * <pre>
 *     foo=float
 *     foo=double
 *     bar=byte
 *     bar=short
 *     bar=int
 *     bar=long
 * </pre>
 * as the output.</p>
 *
 * <h4>Class Templates</h4>
 * <p>Class templates are a single variable list and a file name template followed by a file content template.
 * For example:
 * <pre>
 *     f; $0+File.java#
 *     foo=$0-
 * </pre>
 * will generate two files: {@code FloatFile.java} and {@code DoubleFile.java} which contain the text {@code foo=float}
 * and {@code foo=double}, respectively.</p>
 */
public final class PrimitiveTemplateWriter
{
    private PrimitiveTemplateWriter() {}
    
    static int skipWS(final BufferedInputStream reader) throws IOException
    {
        int c;
        do c = reader.read();
        while(c != -1 && Character.isWhitespace(c));
        return c;
    }
    static final String[][] TYPES =
    {
        {"byte","short","int","long","float","double"},
        {"Byte","Short","Int","Long","Float","Double"}
    };
    record Variables(byte[] limit,byte[] base,byte[] index) {}
    record Template(String[] segments,int[] vars) {}
    static Variables readVariables(final BufferedInputStream reader) throws IOException
    {
        final List<Byte> decl = new ArrayList<>();
        int c;
        byte var = 0;
        do
        {
            switch(c = skipWS(reader))
            {
                case 'i': var |= 1; break;
                case 'f': var |= 2; break;
                case ',':
                case ';':
                    if(var != 0)
                        decl.add(var);
                    var = 0;
                    break;
                case -1:
                    return null;
                default:
                    throw new IllegalArgumentException("unexpected character: '" + Character.toString(c) + "'");
            }
        }
        while(c != ';');
        final byte[] limit = new byte[decl.size()],
                      base = new byte[limit.length],
                     index = new byte[limit.length];
        int i = 0;
        for(final byte b : decl)
        {
            limit[i] = (byte)(((b & 1) << 2) | (b & 2));
            index[i] = base[i] = (byte)((~b & 1) << 2);
            ++i;
        }
        return new Variables(limit,base,index);
    }
    static Template readTemplate(final BufferedInputStream reader) throws IOException
    {
        int c = skipWS(reader);
        if(c == -1) return null;
        final List<String> segments = new ArrayList<>();
        final List<Integer> variables = new ArrayList<>();
        boolean escape = false;
        final StringBuilder builder = new StringBuilder();
        while(c != -1 & (escape | c != '#'))
        {
            if(escape) escape = false;
            else switch(c)
            {
                case '\\' -> escape = true;
                case '$' ->
                {
                    int variable = 0;
                    c = reader.read();
                    while(c != '+' & c != '-')
                    {
                        variable = variable * 10 - '0' + c;
                        c = reader.read();
                    }
                    variables.add((variable << 1) | (c == '-'? 0 : 1));
                    segments.add(builder.toString());
                    builder.delete(0,builder.length());
                }
                default -> builder.appendCodePoint(c);
            }
            c = reader.read();
        }
        segments.add(builder.toString());
        return new Template(segments.toArray(String[]::new),variables.stream().mapToInt(Integer::intValue).toArray());
    }
    @FunctionalInterface interface Writer {void accept(String s) throws IOException;}
    static void executeTemplate(final Variables variables,final Template template,final Writer out) throws IOException
    {
        for(int i = 0;i < template.vars.length;++i)
        {
            out.accept(template.segments[i]);
            final int insert = template.vars[i];
            out.accept(TYPES[insert & 1][variables.index[insert >>> 1]]);
        }
        out.accept(template.segments[template.vars.length]);
    }
    static boolean incrementVariables(final Variables variables)
    {
        int i;
        for(i = 0;i < variables.index.length;++i)
        {
            if(++variables.index[i] < variables.limit[i])
                return false;
            variables.index[i] = variables.base[i];
        }
        return i == variables.index.length;
    }
    
    /**
     * Executes a template on a primitive template class. The template should declare one set of variables, a templated file name,
     * and the templated file contents.
     */
    public static void executeClass(final Path templatePath) throws IOException
    {
        final Variables variables;
        final Template filename,content;
        try(final BufferedInputStream reader = new BufferedInputStream(new FileInputStream(templatePath.toFile())))
        {
            variables = readVariables(reader);
            filename = readTemplate(reader);
            content = readTemplate(reader);
        }
        if(variables == null)
            throw new IllegalArgumentException("missing variables");
        if(filename == null)
            throw new IllegalArgumentException("missing file name");
        if(content == null)
            throw new IllegalArgumentException("missing content");
        
        final Path parent = templatePath.getParent();
        while(true)
        {
            final StringBuilder fn = new StringBuilder();
            executeTemplate(variables,filename,fn::append);
            
            try(final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(parent.resolve(fn.toString()).toFile())))
            {
                executeTemplate(variables,content,s -> out.write(s.getBytes(StandardCharsets.UTF_8)));
            }
            
            if(incrementVariables(variables))
                return;
        }
    }
    /**
     * Executes a template on static functions. The output file will share the name of the template file. Each template
     * section should declare its own variables.
     */
    public static void executeStatic(final Path templatePath) throws IOException
    {
        final Variables[] variables;
        final Template[] templates;
        try(final BufferedInputStream reader = new BufferedInputStream(new FileInputStream(templatePath.toFile())))
        {
            final List<Variables> vars = new ArrayList<>();
            final List<Template> temps = new ArrayList<>();
            Variables v = readVariables(reader);
            Template t = readTemplate(reader);
            while(v != null & t != null)
            {
                vars.add(v);
                temps.add(t);
                v = readVariables(reader);
                t = readTemplate(reader);
            }
            variables = vars.toArray(Variables[]::new);
            templates = temps.toArray(Template[]::new);
        }
        
        final String filename;
        {
            final String tmp = templatePath.getFileName().toString();
            final int i = tmp.lastIndexOf('.');
            filename = i == -1? tmp : tmp.substring(0,i);
        }
        
        try
        (
            final BufferedOutputStream out = new BufferedOutputStream
            (
                new FileOutputStream(templatePath.getParent().resolve(filename + ".java").toFile())
            )
        )
        {
            for(int i = 0;i < variables.length;++i)
                do executeTemplate(variables[i],templates[i],s -> out.write(s.getBytes(StandardCharsets.UTF_8)));
                while(!incrementVariables(variables[i]));
        }
    }
}
