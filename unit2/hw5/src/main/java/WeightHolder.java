public class WeightHolder {
    private int weight;

    public WeightHolder() {
        weight = 0;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void subtractWeight(int weight) {
        this.weight -= weight;
    }

    public void addWeight(int weight) {
        this.weight += weight;
    }
}
