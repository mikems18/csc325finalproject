package fastcards;

import java.util.*;

public class FlashStudySession {

    private final List<Card> cards;
    private final Queue<Integer> queue = new ArrayDeque<>();
    private int totalAttempts = 0;
    private int totalCorrect = 0;
    private final Set<Integer> seen = new HashSet<>();

    public FlashStudySession(Deck deck) {
        this.cards = new ArrayList<>(deck.getCards());
        for (int i = 0; i < cards.size(); i++) {
            queue.add(i);
        }
    }

    public boolean hasNext() {
        return !queue.isEmpty();
    }

    public Card nextCard() {
        Integer idx = queue.peek();
        if (idx == null) return null;
        seen.add(idx);
        return cards.get(idx);
    }

    public void answer(boolean correct) {
        Integer idx = queue.poll();
        if (idx == null) return;
        totalAttempts++;
        if (correct) {
            totalCorrect++;
        } else {
            queue.add(idx);
        }
    }

    public int getTotalAttempts() { return totalAttempts; }

    public int getTotalCorrect() { return totalCorrect; }

    public int getUniqueCardCount() { return seen.size(); }
}
