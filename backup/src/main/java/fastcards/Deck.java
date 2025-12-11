package fastcards;

import java.util.ArrayList;
import java.util.List;

public class Deck {
    private String id;
    private String title;
    private String description;
    private List<Card> cards = new ArrayList<>();

    public Deck() {}

    public Deck(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Card> getCards() { return cards; }
    public void setCards(List<Card> cards) { this.cards = cards; }

    @Override
    public String toString() {
        return title == null ? "(untitled deck)" : title;
    }
}
