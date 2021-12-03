package io.lkakulia.betterreads.search;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SearchController {

    @Value("${openlibrary.cover-image.base}")
    private String COVER_IMAGE_BASE;

    private final WebClient webClient;

    public SearchController(WebClient.Builder webClientBuilder) {
        webClient = webClientBuilder.exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs()
                                .maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .baseUrl("http://openlibrary.org/search.json")
                .build();
    }

    @GetMapping("/search")
    public String getSearchResults(@RequestParam String query, Model model) {
        Mono<SearchResult> resultMono = webClient.get()
                .uri("?q={query}", query)
                .retrieve()
                .bodyToMono(SearchResult.class);
        SearchResult result = resultMono.block();
        List<SearchResultBook> books = result.getDocs()
                .stream()
                .limit(10)
                .map(bookResult -> {
                    bookResult.setKey(bookResult.getKey().replace("/works/", ""));
                    String coverId = bookResult.getCover_i();
                    if (StringUtils.hasText(coverId)) {
                        coverId = COVER_IMAGE_BASE + coverId + "-M.jpg";
                    } else {
                        coverId = "/images/no-image.png";
                    }
                    bookResult.setCover_i(coverId);
                    return bookResult;
                })
                .collect(Collectors.toList());

        model.addAttribute("searchResults", books);

        return "search";
    }

}
