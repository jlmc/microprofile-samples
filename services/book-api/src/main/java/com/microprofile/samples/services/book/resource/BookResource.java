package com.microprofile.samples.services.book.resource;

import com.microprofile.samples.services.book.entity.Book;
import com.microprofile.samples.services.book.model.BookCreate;
import com.microprofile.samples.services.book.model.BookRead;
import com.microprofile.samples.services.book.model.BookUpdate;
import com.microprofile.samples.services.book.persistence.BookRepository;
import com.microprofile.samples.services.book.tracer.TraceLog;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.created;

@ApplicationScoped
public class BookResource implements BookApi {
    @Context
    UriInfo uriInfo;
    @Inject
    BookRepository bookRepository;

    @Override
    public Response get(final Long id) {
        return bookRepository.find(id)
                             .map(Book::toBookRead)
                             .map(Response::ok)
                             .orElse(Response.status(NOT_FOUND))
                             .build();
    }

    @Override
    public Response get() {
        final List<BookRead> books =
            bookRepository.find()
                          .stream()
                          .map(Book::toBookRead)
                          .collect(toList());
        return Response.ok(books).build();
    }

    @Override
    @RolesAllowed("admin")
    @TraceLog
    @Counted(name = "booksCreationCount")
    @Metered(name = "booksCreationMeter")
    @Timed(name = "booksCreationTime")
    public Response create(final BookCreate bookCreate) {
        return bookRepository.create(bookCreate.toBook())
                             .map(Book::toBookRead)
                             .map(book -> created(
                                 uriInfo.getRequestUriBuilder().path("{id}").build(book.getId())).entity(book))
                             .orElse(Response.status(NOT_FOUND))
                             .build();
    }

    @Override
    @RolesAllowed("admin")
    @TraceLog
    @Counted(name = "booksUpdateCount")
    @Metered(name = "booksUpdateMeter")
    @Timed(name = "booksUpdateTime")
    public Response update(final Long id, final BookUpdate bookUpdate) {
        return bookRepository.update(id, bookUpdate.toBook())
                             .map(Book::toBookRead)
                             .map(Response::ok)
                             .orElse(Response.status(NOT_FOUND))
                             .build();

    }

    @Override
    @RolesAllowed("admin")
    @TraceLog
    @Counted(name = "booksDeleteCount")
    @Metered(name = "booksDeleteMeter")
    @Timed(name = "booksDeleteTime")
    public Response delete(final Long id) {
        return bookRepository.delete(id)
                             .map(book -> Response.noContent())
                             .orElse(Response.status(NOT_FOUND))
                             .build();
    }

    @Gauge(name = "booksWithoutIsbn", unit = MetricUnits.NONE)
    public Long countWithoutIsbn() {
        return bookRepository.countWithoutIsbn();
    }
}
