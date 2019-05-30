package sample;
// https://stackoverflow.com/questions/28843858/javafx-8-listview-with-checkboxes

import javafx.beans.property.*;

public class ListViewCheckItem {
    private final StringProperty name = new SimpleStringProperty();
    private final BooleanProperty on = new SimpleBooleanProperty();
    private final IntegerProperty number = new SimpleIntegerProperty();
    private String[] ids;

    public ListViewCheckItem(String name, boolean on, int numOfTracks, String[] newIDs) {
        setName(name);
        setOn(on);
        setTracks(numOfTracks);
        ids = newIDs;
    }

    public final StringProperty nameProperty() {
        return this.name;
    }

    public final String getName() {
        return this.nameProperty().get();
    }

    public final void setName(final String name) {
        this.nameProperty().set(name);
    }

    public final BooleanProperty onProperty() {
        return this.on;
    }

    public final boolean isOn() {
        return this.onProperty().get();
    }

    public final void setOn(final boolean on) {
        this.onProperty().set(on);
    }

    public final IntegerProperty intProperty() {
        return this.number;
    }

    public final Integer getTracks() {
        return this.intProperty().get();
    }

    public final void setTracks(final Integer tracks) {
        this.intProperty().set(tracks);
    }

    public String[] getIDs(){
        return ids;
    }

    @Override
    public String toString() {
        return getName() + ":    " + getTracks() + " files";
    }

}
