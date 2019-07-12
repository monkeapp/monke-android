package io.monke.app.setup.adapters;

public class GuideItem {
    public int number;
    public CharSequence title;
    public CharSequence actionTitle;
    public CharSequence actionSecondTitle;

    public GuideItem(int number, CharSequence title, CharSequence actionTitle) {
        this.number = number;
        this.title = title;
        this.actionTitle = actionTitle;
    }

    public GuideItem(int number, CharSequence title, CharSequence actionTitle, CharSequence actionSecondTitle) {
        this(number, title, actionTitle);
        this.actionSecondTitle = actionSecondTitle;
    }
}
