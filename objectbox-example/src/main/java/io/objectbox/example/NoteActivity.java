package io.objectbox.example;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.query.Query;

public class NoteActivity extends Activity {

    private EditText editText;
    private View addNoteButton;

    private Box<Note> notesBox;
    private Query<Note> notesQuery;
    private NotesAdapter notesAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setUpViews();

        // get the note box
        notesBox = ((App) getApplication()).getBoxStore().boxFor(Note.class);

        // query all notes, sorted a-z by their text
        notesQuery = notesBox.query().order(Note_.text).build();
        updateNotes();
    }

    private void updateNotes() {
        List<Note> notes = notesQuery.find();
        notesAdapter.setNotes(notes);
    }

    protected void setUpViews() {
        ListView listView = (ListView) findViewById(R.id.listViewNotes);
        listView.setOnItemClickListener(noteClickListener);

        notesAdapter = new NotesAdapter();
        listView.setAdapter(notesAdapter);

        addNoteButton = findViewById(R.id.buttonAdd);
        addNoteButton.setEnabled(false);

        editText = (EditText) findViewById(R.id.editTextNote);
        editText.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addNote();
                    return true;
                }
                return false;
            }
        });
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean enable = s.length() != 0;
                addNoteButton.setEnabled(enable);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void onAddButtonClick(View view) {
        addNote();
    }

    private void addNote() {
        String noteText = editText.getText().toString();
        editText.setText("");

        final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        String comment = "Added on " + df.format(new Date());

        Note note = new Note(0, noteText, comment, new Date());
        notesBox.put(note);
        Log.d(App.TAG, "Inserted new note, ID: " + note.getId());

        updateNotes();
    }

    OnItemClickListener noteClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Note note = notesAdapter.getItem(position);
            Long noteId = note.getId();

            notesBox.remove(note);
            Log.d(App.TAG, "Deleted note, ID: " + noteId);

            updateNotes();
        }
    };
}