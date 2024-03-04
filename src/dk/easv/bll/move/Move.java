package dk.easv.bll.move;

import java.util.Objects;

public class Move implements IMove{
    int x=0;
    int y=0;

    public Move(int x, int y) {
        this.x=x;
        this.y=y;
    }

    public void setY(int y){
        this.y=y;
    }

    public void setX(int x){
        this.x=x;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "("+x+","+y+")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return x == move.x && y == move.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
