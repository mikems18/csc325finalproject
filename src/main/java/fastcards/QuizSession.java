package fastcards;

import java.util.*;

public class QuizSession {
    public enum Mode { FLASHCARDS, MULTIPLE_CHOICE }
    private final Deck deck;
    private final Mode mode;
    private final int choices;
    private int index = 0;
    private int correct = 0;

    public QuizSession(Deck deck, Mode mode, int choices){
        this.deck = deck; this.mode = mode; this.choices = choices;
    }

    public Deck deck(){ return deck; }
    public Mode mode(){ return mode; }
    public int total(){ return deck.size(); }
    public int index(){ return index; }
    public boolean isLast(){ return index >= total()-1; }
    public Flashcard current(){ return deck.getCards().get(index); }
    public void next(){ if (!isLast()) index++; }
    public void prev(){ if (index > 0) index--; }
    public void markAnswer(String text){ if (current().answer().equals(text)) correct++; }
    public int correct(){ return correct; }
    public String posLabel(){ return (index+1) + " / " + total(); }

    public List<String> buildChoices(){
        Set<String> out = new LinkedHashSet<>();
        out.add(current().answer());
        Random r = new Random();
        var cards = deck.getCards();
        while (out.size() < Math.min(choices, cards.size())) {
            var c = cards.get(r.nextInt(cards.size()));
            out.add(c.answer());
        }
        var list = new ArrayList<>(out);
        Collections.shuffle(list);
        return list;
    }
}
