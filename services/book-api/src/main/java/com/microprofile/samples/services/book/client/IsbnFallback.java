package com.microprofile.samples.services.book.client;

import com.microprofile.samples.services.book.entity.Book;
import com.microprofile.samples.services.book.persistence.BookRepository;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class IsbnFallback {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Inject
    BookRepository bookRepository;

    @Incoming("books")
    public CompletionStage<Void> fallback(final Book book) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        executor.schedule(() -> {
            book.setIsbn(generate());
            bookRepository.update(book.getId(), book);
            future.complete(null);
        }, 5, TimeUnit.SECONDS);

        return future;
    }

    private String generate() {
        return "ISBN" + "-" + (int) Math.floor((Math.random() * 9999999)) + 1;
    }
}
