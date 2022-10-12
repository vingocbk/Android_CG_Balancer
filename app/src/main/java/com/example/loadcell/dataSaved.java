package com.example.loadcell;

public class dataSaved {
    private int id;
    private int X;
    private int Y;
    private int Z;
    private String name;

    public dataSaved(int id, String name, int X, int Y, int Z) {
        this.id = id;
        this.name = name;
        this.X = X;
        this.Y = Y;
        this.Z = Z;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getX() {
        return X;
    }

    public void setX(int X) {
        this.X = X;
    }

    public int getY() {
        return Y;
    }

    public void setY(int Y) {
        this.Y = Y;
    }

    public int getZ() {
        return Z;
    }

    public void setZ(int Z) {
        this.Z = Z;
    }
}
