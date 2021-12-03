package io.lkakulia.betterreads.book;

import io.lkakulia.betterreads.book.Book;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends CassandraRepository<Book, String> {
}
