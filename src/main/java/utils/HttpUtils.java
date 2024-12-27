package utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HttpUtils
{
    private HttpUtils() {}
    
    public static String httpGet(final String url)
    {
        try(final HttpClient client = HttpClient.newHttpClient())
        {
            return client.send
            (
                HttpRequest
                    .newBuilder(URI.create(url))
                    .header("cookie",Files.readString(Path.of("src","main","resources","cookie")))
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            ).body();
        }
        catch(InterruptedException|IOException e) {throw new RuntimeException(e);}
    }
}
