package io.lkakulia.betterreads.home;

import io.lkakulia.betterreads.user.BooksByUser;
import io.lkakulia.betterreads.user.BooksByUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Value("${openlibrary.cover-image.base}")
    private String COVER_IMAGE_BASE;

    private final BooksByUserRepository booksByUserRepository;

    public HomeController(@Autowired BooksByUserRepository booksByUserRepository) {
        this.booksByUserRepository = booksByUserRepository;
    }

    @GetMapping("/")
    public String home(
            @AuthenticationPrincipal OAuth2User principal,
            Model model
    ) {
        if (principal == null || principal.getAttribute("login") == null) {
            return "index";
        }

        String userId = principal.getAttribute("login");
        Slice<BooksByUser> booksSlice = booksByUserRepository.findAllById(userId, CassandraPageRequest.of(0, 100));
        List<BooksByUser> booksByUser = booksSlice.getContent();
        booksByUser = booksByUser.stream()
                .distinct()
                .map(book -> {
                    String coverImageUrl = "/images/no-image.png";
                    if (book.getCoverIds() != null && book.getCoverIds().size() > 0) {
                        coverImageUrl = COVER_IMAGE_BASE + book.getCoverIds().get(0) + "-L.jpg";
                    }
                    book.setCoverUrl(coverImageUrl);
                    return book;
                })
                .collect(Collectors.toList());

        model.addAttribute("books", booksByUser);

        return "home";
    }

}
