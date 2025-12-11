package fastcards;

import java.util.*;

public class MultipleChoiceStudySession {

    private final List<Card> cards;
    private int index = 0;
    private int correctCount = 0;

    public MultipleChoiceStudySession(Deck deck) {
        this.cards = new ArrayList<>(deck.getCards());
        Collections.shuffle(this.cards);
    }

    public boolean hasNext() {
        return index < cards.size();
    }

    public Card current() {
        if (!hasNext()) return null;
        return cards.get(index);
    }

    public List<String> buildChoices() {
        Card current = current();
        if (current == null) return List.of();
        String correct = current.getBack();
        List<String> others = new ArrayList<>();
        for (Card c : cards) {
            if (c == current) continue;
            String ans = c.getBack();
            if (ans != null && !ans.isBlank() && !ans.equals(correct) && !others.contains(ans)) {
                others.add(ans);
            }
        }
        Collections.shuffle(others);
        List<String> result = new ArrayList<>();
        result.add(correct);
        for (int i = 0; i < Math.min(3, others.size()); i++) {
            result.add(others.get(i));
        }
        Collections.shuffle(result);
        return result;
    }

    public void markAnswer(boolean correct) {
        if (correct) correctCount++;
        index++;
    }

    public int getCorrectCount() { return correctCount; }

    public int getTotal() { return cards.size(); }
}
