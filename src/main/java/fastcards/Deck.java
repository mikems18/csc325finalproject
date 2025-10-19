package fastcards;

import java.util.*;

public class Deck {
    private final int id;
    private final String name;
    private final Integer ownerId; // null -> public deck
    private List<Flashcard> cards = new ArrayList<>();

    public Deck(int id, String name, Integer ownerId){
        this.id = id; this.name = name; this.ownerId = ownerId;
    }

    public int getId(){ return id; }
    public String getName(){ return name; }
    public Integer getOwnerId(){ return ownerId; }
    public List<Flashcard> getCards(){ return Collections.unmodifiableList(cards); }
    public void setCards(List<Flashcard> list){ cards = new ArrayList<>(list); }
    public int size(){ return cards.size(); }
    @Override public String toString(){ return name + " (" + size() + ")"; }
}
