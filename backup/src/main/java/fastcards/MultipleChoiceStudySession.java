package fastcards;

import java.util.*;

/**
 * Multiple-choice study session.
 * Each card is used once; no recycling of wrong answers (simpler).
 */
public class MultipleChoiceStudySession {

    private final Deck deck;
    private final List<Card> cards;
    private int index = 0;
    private int correctCount = 0;

    public MultipleChoiceStudySession(Deck deck) {
        this.deck = deck;
        this.cards = deck.getCards();
    }

    public boolean hasNext() {
        return index < cards.size();
    }

    public Card current() {
        return cards.get(index);
    }

    public void markAnswer(boolean correct) {
        if (correct) correctCount++;
        index++;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getTotal() {
        return cards.size();
    }

    /**
     * Build four options: correct answer + up to 3 wrong answers
     * drawn from other cards in the same deck.
     */
    public List<String> buildChoices() {
        String correctAnswer = current().getBack();
        Set<String> out = new LinkedHashSet<>();
        out.add(correctAnswer);

        List<String> candidates = new ArrayList<>();
        for (Card c : cards) {
            if (c == current()) continue;
            String ans = c.getBack();
            if (ans == null) continue;
            ans = ans.trim();
            if (ans.isEmpty()) continue;
            if (ans.equals(correctAnswer)) continue;
            if (!candidates.contains(ans)) {
                candidates.add(ans);
            }
        }

        Collections.shuffle(candidates, new Random());
        int neededWrong = Math.min(3, candidates.size());
        for (int i = 0; i < neededWrong; i++) {
            out.add(candidates.get(i));
        }

        List<String> list = new ArrayList<>(out);
        Collections.shuffle(list, new Random());
        return list;
    }
}
