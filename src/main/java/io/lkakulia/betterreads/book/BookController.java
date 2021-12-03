package io.lkakulia.betterreads.book;


import io.lkakulia.betterreads.userbooks.UserBooks;
import io.lkakulia.betterreads.userbooks.UserBooksPrimaryKey;
import io.lkakulia.betterreads.userbooks.UserBooksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class BookController {

    @Value("${openlibrary.cover-image.base}")
    private String COVER_IMAGE_BASE;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    UserBooksRepository userBooksRepository;

    @GetMapping("/books/{bookId}")
    public String getBook(@PathVariable String bookId, Model model,
                          @AuthenticationPrincipal OAuth2User principal) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);

        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            String coverImageUrl = "/images/no-image.png";
            if (book.getCoverIds() != null && !book.getCoverIds().isEmpty()) {
                coverImageUrl = COVER_IMAGE_BASE + book.getCoverIds().get(0) + "-L.jpg";
            }
            model.addAttribute("coverImage", coverImageUrl);
            model.addAttribute("book", book);

            if (principal != null && principal.getAttribute("login") != null) {
                String userId = principal.getAttribute("login");
                model.addAttribute("loginId", userId);
                UserBooksPrimaryKey key = new UserBooksPrimaryKey();
                key.setBookId(bookId);
                key.setUserId(userId);
                Optional<UserBooks> optionalUserBooks = userBooksRepository.findById(key);
                optionalUserBooks.ifPresentOrElse(userBooks -> model.addAttribute("userBooks", userBooks),
                        () -> model.addAttribute("userBooks", new UserBooks()));
            }

            return "book";
        }

        return "book-not-found";
    }

}
