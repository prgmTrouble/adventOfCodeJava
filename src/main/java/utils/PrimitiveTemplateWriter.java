package utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
        do
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
        while(c != -1 & (escape | c != '#'));
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
        for(int i = 0;i < variables.index.length;++i)
        {
            if(++variables.index[i] < variables.limit[i])
                return false;
            variables.index[i] = variables.base[i];
        }
        return true;
    }
    
    /**
     * Executes a template
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
            while(v != null && t != null)
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
                while(incrementVariables(variables[i]));
        }
    }
}
