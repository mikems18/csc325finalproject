package fastcards;

import java.util.*;

/**
 * Flashcard study session:
 * - Shows question (front)
 * - Then flips to show answer (back)
 * - If user marks incorrect, that card is recycled back into the queue.
 */
public class FlashStudySession {

    private final Deck deck;
    private final List<Card> cards;
    private final Deque<Integer> queue = new ArrayDeque<>();
    private int currentIndex = -1;

    private int totalAttempts = 0;
    private int totalCorrect = 0;

    public FlashStudySession(Deck deck) {
        this.deck = deck;
        this.cards = deck.getCards();
        for (int i = 0; i < cards.size(); i++) {
            queue.addLast(i);
        }
    }

    public boolean hasNext() {
        return !queue.isEmpty();
    }

    public Card nextCard() {
        if (queue.isEmpty()) {
            currentIndex = -1;
            return null;
        }
        currentIndex = queue.removeFirst();
        return cards.get(currentIndex);
    }

    /**
     * Record user's answer for the current card.
     * @param correct true if user got it right; false to recycle card.
     */
    public void answer(boolean correct) {
        if (currentIndex < 0) return;
        totalAttempts++;
        if (correct) {
            totalCorrect++;
            // do not re-add
        } else {
            // recycle card to end of queue
            queue.addLast(currentIndex);
        }
        currentIndex = -1;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public int getTotalCorrect() {
        return totalCorrect;
    }

    public int getUniqueCardCount() {
        return cards.size();
    }
}
