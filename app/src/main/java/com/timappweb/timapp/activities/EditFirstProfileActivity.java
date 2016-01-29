package com.timappweb.timapp.activities;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.List;

public class EditFirstProfileActivity extends BaseActivity{

    String TAG = "EditProfileActivity";
    private Activity activity = this;

    private HorizontalTagsRecyclerView horizontalTagsRecyclerView;
    private HorizontalTagsAdapter horizontalTagsAdapter;
    private EditText editText;
    private Button skipButton;
    private Button buttonSubmit;
    private View submitView;

    private int counterTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_first_profile);
        this.initToolbar(true);

        horizontalTagsRecyclerView = (HorizontalTagsRecyclerView) findViewById(R.id.selected_tags_profile);
        editText = (EditText) findViewById(R.id.edit_text);
        skipButton = (Button) findViewById(R.id.skip_button);
        buttonSubmit = (Button) findViewById(R.id.button_submit);
        submitView = findViewById(R.id.submit_view);

        init();

        initAdapter();
        setListener();

    }

    private void init() {
        counterTags = 0;
        editText.requestFocus();
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }

    private void initAdapter() {
        horizontalTagsAdapter = new HorizontalTagsAdapter(this);
        horizontalTagsRecyclerView.setAdapter(horizontalTagsAdapter);
    }


    private void setListener() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();
                if (string.contains(" ")) {
                    Toast.makeText(EditFirstProfileActivity.this, R.string.toast_no_space, Toast.LENGTH_SHORT).show();
                    string = string.substring(0, string.length()-1);
                    editText.setText(string);
                    editText.setSelection(string.length());
                }
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int length = editText.getText().length();
                if (length == 0) {
                    Toast.makeText(EditFirstProfileActivity.this, R.string.toast_no_tag, Toast.LENGTH_SHORT).show();
                    return false;
                } else if (length == 1) {
                    Toast.makeText(EditFirstProfileActivity.this, R.string.toast_tag_one_letter, Toast.LENGTH_SHORT).show();
                    editText.requestFocus();
                    return false;
                } else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onSubmitTag();
                    return true;
                }
                return false;
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Tag> tagList = horizontalTagsAdapter.getData();
                IntentsUtils.profile(activity, tagList);
            }
        });

        horizontalTagsAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Toast.makeText(EditFirstProfileActivity.this, "YOUPI", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onSubmitTag() {

        switch (counterTags) {
            case 0:
                horizontalTagsAdapter.addData(editText.getText().toString());
                horizontalTagsAdapter.notifyDataSetChanged();
                editText.setHint(getResources().getString(R.string.hint_et_edit_first_profile2));
                editText.setText("");
                break;
            case 1:
                horizontalTagsAdapter.addData(editText.getText().toString());
                horizontalTagsAdapter.notifyDataSetChanged();
                editText.setHint(getResources().getString(R.string.hint_et_edit_first_profile3));
                editText.setText("");
                break;
            case 2:
                horizontalTagsAdapter.addData(editText.getText().toString());
                horizontalTagsAdapter.notifyDataSetChanged();

                //Hide keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                editText.setVisibility(View.GONE);
                skipButton.setVisibility(View.GONE);
                submitView.setVisibility(View.VISIBLE);
                break;
        }
        counterTags= counterTags + 1;
    }

}
