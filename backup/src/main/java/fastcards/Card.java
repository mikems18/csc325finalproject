package fastcards;

public class Card {
    private String id;
    private String front;
    private String back;

    public Card() {}

    public Card(String id, String front, String back) {
        this.id = id;
        this.front = front;
        this.back = back;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFront() { return front; }
    public void setFront(String front) { this.front = front; }

    public String getBack() { return back; }
    public void setBack(String back) { this.back = back; }

    @Override
    public String toString() {
        return front == null ? "" : front;
    }
}
