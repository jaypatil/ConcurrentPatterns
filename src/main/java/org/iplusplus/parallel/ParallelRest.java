package org.iplusplus.parallel;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ParallelRest {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        URI uri = null;
        try {
            uri = new URI("https://www.google.com/");
            Request<String> request = new Request<>();
            request.setUri(uri);

            List<CompletableFuture<Response<String>>> responses = new ArrayList<>();

            for (int i = 0; i < 150; i++) {
                CompletableFuture<Response<String>> future = CompletableFuture.supplyAsync(request, executor);
                responses.add(future);
            }
            CompletableFuture<List<Response<String>>> allResponse = CompletableFuture.allOf(responses.toArray(new CompletableFuture[responses.size()])).thenApply(v -> responses.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList()));
            for (Response<String> response :
                    allResponse.getNow(new ArrayList<>()) ){
                if (null != response) {
                    System.out.println("#########################################");
                    System.out.println(response.getBody());
                    System.out.println("#########################################");
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }
}

class Request<T> implements Supplier<Response<T>> {

    private static RestTemplate restTemplate = new RestTemplate();

    private URI uri;
    private Class<T> type;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public Response<T> get() {
        ResponseEntity<T> response;
        try {
            TimeUnit.SECONDS.sleep(1);
            Class<ResponseEntity<T>> clazz;
            response = restTemplate.getForEntity(uri, getType());
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return new Response(response.getBody(), response.getStatusCode());
    }

}

class Response<T> extends ResponseEntity<T> {

    public Response(HttpStatus status) {
        super(status);
    }

    public Response(T body, HttpStatus status) {
        super(body, status);
    }

    public Response(MultiValueMap<String, String> headers, HttpStatus status) {
        super(headers, status);
    }

    public Response(T body, MultiValueMap<String, String> headers, HttpStatus status) {
        super(body, headers, status);
    }
}