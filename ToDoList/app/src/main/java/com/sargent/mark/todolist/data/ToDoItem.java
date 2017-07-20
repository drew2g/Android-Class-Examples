package com.sargent.mark.todolist.data;

/**
 * Created by mark on 7/4/17.
 */

public class ToDoItem {
    private String description;
    private String dueDate;
    private int isDone;
    private String category;

    public ToDoItem(String description, String dueDate, String category, int isDone) {
        this.description = description;
        this.dueDate = dueDate;
        this.isDone = isDone;
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public int getIsDone() { return isDone; }

    public void setIsDone(Integer isDone) { this.isDone = isDone; }

    public String getCategory() { return category; }

    public void setCategory( String category ) { this.category = category; }
}
