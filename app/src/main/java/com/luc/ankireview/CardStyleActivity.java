package com.luc.ankireview;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.luc.ankireview.style.CardField;
import com.luc.ankireview.style.CardStyle;
import com.luc.ankireview.style.CardTemplate;
import com.luc.ankireview.style.CardTemplateKey;
import com.luc.ankireview.style.FieldListAdapter;
import com.luc.ankireview.style.ItemTouchCallback;
import com.luc.ankireview.style.ValueSlider;
import com.luc.ankireview.style.ValueSliderUpdate;
import com.thebluealliance.spectrum.SpectrumDialog;
import com.thebluealliance.spectrum.SpectrumPalette;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.util.ArrayList;
import java.util.Vector;

public class CardStyleActivity extends AppCompatActivity {
    private static final String TAG = "CardStyleActivity";
    public static final double TEXT_RELATIVE_SIZE_FACTOR = 10.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardstyle);


        m_cardStyle = new CardStyle(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.review_toolbar);
        toolbar.setTitle(R.string.card_style);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        long noteId = intent.getLongExtra("noteId", 0l);
        int cardOrd = intent.getIntExtra("cardOrd", 0);

        Log.v(TAG, "starting CardStyleActivity with noteId: " + noteId + " cardOrd: " + cardOrd);

        // retrieve the appropriate card
        m_card = AnkiUtils.retrieveCard(getContentResolver(), noteId, cardOrd);

        // retrieve the card template
        m_cardTemplateKey = new CardTemplateKey(m_card.getModelId(), m_card.getCardOrd());
        m_cardTemplate = m_cardStyle.getCardTemplate(m_cardTemplateKey);
        if(m_cardTemplate == null) {
            // create a new one
            m_cardTemplate = m_cardStyle.createCardTemplate(m_cardTemplateKey);
            showDefineStyleDialog();
        }

        // Log.v(TAG, "num question card fields: " + m_cardTemplate.getQuestionCardFields().size());

        m_bottomNavigation = findViewById(R.id.cardstyle_bottom_navigation);
        m_bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.cardstyle_action_fields:
                        showFieldListView();
                        break;
                    case R.id.cardstyle_action_field_settings:
                        showFieldSettingsView();
                        break;
                    case R.id.cardstyle_action_font:
                        showFontView();
                        break;
                    case R.id.cardstyle_action_spacing:
                        showSpacingView();
                        break;
                }
                return true;
            }
        });

        m_cardstyleEditorCards = findViewById(R.id.cardstyle_editor_cards);
        m_cardStyle.renderCard(m_card, m_cardstyleEditorCards);

        // get font view and margins view
        m_fieldSettingsView = findViewById(R.id.cardstyle_editor_fieldsettings);
        m_fontView = findViewById(R.id.cardstyle_editor_font);
        m_marginsView = findViewById(R.id.cardstyle_editor_margins);

        // setup the full Field list ListView
        m_fullFieldListView = findViewById(R.id.cardstyle_editor_all_fields);
        m_fullFieldListView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        m_fullFieldListView.setLayoutManager(linearLayoutManager);

        // get field list
        Vector<String> fullFieldList = new Vector<String>();
        for (String field : m_card.getFieldMap().keySet())
        {
            fullFieldList.add(field);
        }

        m_fieldListAdapter = new FieldListAdapter(this, m_cardTemplate, fullFieldList);
        m_fullFieldListView.setAdapter(m_fieldListAdapter);

        ItemTouchCallback itemTouchCallback = new ItemTouchCallback(m_fieldListAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(itemTouchCallback);
        m_fieldListAdapter.setTouchHelper(touchHelper);
        touchHelper.attachToRecyclerView(m_fullFieldListView);

        // set visibility of tabs
        m_fullFieldListView.setVisibility(View.VISIBLE);
        m_fieldSettingsView.setVisibility(View.INVISIBLE);
        m_fontView.setVisibility(View.INVISIBLE);
        m_marginsView.setVisibility(View.INVISIBLE);

        // field settings controls
        // =======================

        // field selector
        m_fieldSpinner = findViewById(R.id.cardstyle_field_settings_selector);
        m_fieldSpinnerAdapter = new ArrayAdapter<CardField>(this, R.layout.simple_spinner_item);
        m_fieldSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_fieldSpinner.setAdapter(m_fieldSpinnerAdapter);
        m_fieldSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                CardField cardField = (CardField) adapterView.getItemAtPosition(i);
                openFieldSettings(cardField);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // relative size
        m_field_relativesize = findViewById(R.id.cardstyle_text_relativesize_valueslider);
        m_field_relativesize.setListener(new ValueSliderUpdate() {
            @Override
            public void valueUpdate(int currentValue) {
                m_currentCardField.setRelativeSize((float) (currentValue / TEXT_RELATIVE_SIZE_FACTOR));
                updateCardPreview();
            }
        });

        // line return
        m_lineReturnCheckBox = findViewById(R.id.cardstyle_field_linereturn);
        m_lineReturnCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                m_currentCardField.setLineReturn(b);
                updateCardPreview();
            }
        });

        // html
        m_htmlCheckBox = findViewById(R.id.cardstyle_field_html);
        m_htmlCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                m_currentCardField.setIsHtml(b);
                updateCardPreview();
            }
        });

        // alignment
        m_fieldAlignmentSpinner = findViewById(R.id.cardstyle_field_alignment);
        m_fieldAlignmentAdapter = ArrayAdapter.createFromResource(this,
                R.array.alignment_array,  R.layout.simple_spinner_item);
        m_fieldAlignmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_fieldAlignmentSpinner.setAdapter(m_fieldAlignmentAdapter);
        m_fieldAlignmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(m_currentCardField == null)
                    return;

                switch(i) {
                    case 0:
                        m_currentCardField.setAlignment(Layout.Alignment.ALIGN_CENTER);
                        break;
                    case 1:
                        m_currentCardField.setAlignment(Layout.Alignment.ALIGN_NORMAL);
                        break;
                    case 2:
                        m_currentCardField.setAlignment(Layout.Alignment.ALIGN_OPPOSITE);
                        break;
                    default:
                        break;
                }
                updateCardPreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // left margin
        m_field_leftmargin = findViewById(R.id.cardstyle_field_leftmargin);
        m_field_leftmargin.setListener(new ValueSliderUpdate() {
            @Override
            public void valueUpdate(int currentValue) {
                m_currentCardField.setLeftMargin(currentValue);
                updateCardPreview();
            }
        });

                // color
        m_field_colorSelector = findViewById(R.id.cardstyle_field_color_selector);
        m_fieldColorCircle = findViewById(R.id.cardstyle_field_color_circle);


        // text controls
        // -------------

        ValueSlider valueSliderTextBaseSize = findViewById(R.id.cardstyle_text_basesize_valueslider);
        valueSliderTextBaseSize.setCurrentValue(m_cardTemplate.getBaseTextSize());
        valueSliderTextBaseSize.setListener(new ValueSliderUpdate() {
            @Override
            public void valueUpdate(int currentValue) {
                m_cardTemplate.setBaseTextSize(currentValue);
                updateCardPreview();
            }
        });

        m_text_font_family = findViewById(R.id.cardstyle_editor_font_family);
        m_text_font_family.setText(m_cardTemplate.getFont());
        m_text_font_lookup = findViewById(R.id.cardstyle_editor_font_lookup);
        m_text_font_lookup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fontFamily = m_text_font_family.getText().toString();
                m_cardTemplate.setFont(fontFamily);
                updateCardPreview();

                hideSoftKeyboard();

            }
        });

        // margin controls
        // ---------------

        ValueSlider valueSliderMarginLeftRight = findViewById(R.id.cardstyle_margin_leftright_valueslider);
        valueSliderMarginLeftRight.setCurrentValue(m_cardTemplate.getLeftRightMargin());
        valueSliderMarginLeftRight.setListener(new ValueSliderUpdate() {
            @Override
            public void valueUpdate(int currentValue) {
                m_cardTemplate.setLeftRightMargin(currentValue);
                updateCardPreview();
            }
        });

        ValueSlider valueSliderBetween = findViewById(R.id.cardstyle_margin_between_valueslider);
        valueSliderBetween.setCurrentValue(m_cardTemplate.getLeftRightMargin());
        valueSliderBetween.setListener(new ValueSliderUpdate() {
            @Override
            public void valueUpdate(int currentValue) {
                m_cardTemplate.setCenterMargin(currentValue);
                updateCardPreview();
            }
        });


        ValueSlider valueSliderPaddingTop = findViewById(R.id.cardstyle_padding_top_valueslider);
        valueSliderPaddingTop.setCurrentValue(m_cardTemplate.getPaddingTop());
        valueSliderPaddingTop.setListener(new ValueSliderUpdate() {
            @Override
            public void valueUpdate(int currentValue) {
                m_cardTemplate.setPaddingTop(currentValue);
                updateCardPreview();
            }
        });


        ValueSlider valueSliderPaddingBottom = findViewById(R.id.cardstyle_padding_bottom_valueslider);
        valueSliderPaddingBottom.setCurrentValue(m_cardTemplate.getPaddingBottom());
        valueSliderPaddingBottom.setListener(new ValueSliderUpdate() {
            @Override
            public void valueUpdate(int currentValue) {
                m_cardTemplate.setPaddingBottom(currentValue);
                updateCardPreview();
            }
        });

        ValueSlider valueSliderPaddingLeftRight = findViewById(R.id.cardstyle_padding_leftright_valueslider);
        valueSliderPaddingLeftRight.setCurrentValue(m_cardTemplate.getPaddingLeftRight());
        valueSliderPaddingLeftRight.setListener(new ValueSliderUpdate() {
            @Override
            public void valueUpdate(int currentValue) {
                m_cardTemplate.setPaddingLeftRight(currentValue);
                updateCardPreview();
            }
        });




    }


    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) this.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                this.getCurrentFocus().getWindowToken(), 0);
    }

    public void showDefineStyleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.card_style_define_style).setTitle(R.string.card_style_not_found).setPositiveButton(R.string.card_style_create_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.cardstyle_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cardstyle_save:
                Log.v(TAG, "save card style");
                saveCardStyle();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void showFieldListView() {
        m_fullFieldListView.setVisibility(View.VISIBLE);
        m_fieldSettingsView.setVisibility(View.INVISIBLE);
        m_fontView.setVisibility(View.INVISIBLE);
        m_marginsView.setVisibility(View.INVISIBLE);
    }

    private void showFieldSettingsView() {
        m_fullFieldListView.setVisibility(View.INVISIBLE);
        m_fieldSettingsView.setVisibility(View.VISIBLE);
        m_fontView.setVisibility(View.INVISIBLE);
        m_marginsView.setVisibility(View.INVISIBLE);

        setupFieldSettings();
    }

    private void showFontView() {
        m_fullFieldListView.setVisibility(View.INVISIBLE);
        m_fieldSettingsView.setVisibility(View.INVISIBLE);
        m_fontView.setVisibility(View.VISIBLE);
        m_marginsView.setVisibility(View.INVISIBLE);
    }

    private void showSpacingView() {
        m_fullFieldListView.setVisibility(View.INVISIBLE);
        m_fieldSettingsView.setVisibility(View.INVISIBLE);
        m_fontView.setVisibility(View.INVISIBLE);
        m_marginsView.setVisibility(View.VISIBLE);
    }

    public void updateCardPreview() {
        m_cardStyle.renderCard(m_card, m_cardstyleEditorCards);
    }

    public void saveCardStyle() {
        m_cardStyle.saveCardStyleData();
        finish();
    }

    private void setupFieldSettings() {
        // setup the field dropdown
        m_fieldSpinnerAdapter.clear();

        for(CardField cardField : m_cardTemplate.getQuestionCardFields()) {
            m_fieldSpinnerAdapter.add(cardField);
        }

        for(CardField cardField : m_cardTemplate.getAnswerCardFields()) {
            m_fieldSpinnerAdapter.add(cardField);
        }

    }

    public void openFieldSettings(final CardField cardField) {
        Log.v(TAG, "openFieldSettings " + cardField.getFieldName());

        m_currentCardField = cardField;

        m_field_relativesize.setCurrentValue((int) (cardField.getRelativeSize() * TEXT_RELATIVE_SIZE_FACTOR));

        m_lineReturnCheckBox.setChecked(m_currentCardField.getLineReturn());

        m_htmlCheckBox.setChecked(m_currentCardField.getIsHtml());

        switch( m_currentCardField.getAlignment()) {
            case ALIGN_CENTER:
                m_fieldAlignmentSpinner.setSelection(0);
                break;
            case ALIGN_NORMAL:
                m_fieldAlignmentSpinner.setSelection(1);
                break;
            case ALIGN_OPPOSITE:
                m_fieldAlignmentSpinner.setSelection(2);
                break;
        }

        m_field_leftmargin.setCurrentValue(m_currentCardField.getLeftMargin());

        final CardStyleActivity context = this;

        ImageViewCompat.setImageTintList(m_fieldColorCircle, ColorStateList.valueOf(cardField.getColor()));
        m_field_colorSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SpectrumDialog.Builder(context)
                        .setColors(R.array.text_field_colors)
                        .setSelectedColor(cardField.getColor())
                        .setDismissOnColorSelected(true)
                        .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                            @Override public void onColorSelected(boolean positiveResult, @ColorInt int color) {
                                if (positiveResult) {
                                    cardField.setColor(color);
                                    ImageViewCompat.setImageTintList(m_fieldColorCircle, ColorStateList.valueOf(color));
                                    updateCardPreview();
                                }
                            }
                        }).build().show(context.getSupportFragmentManager(), "field_color_picker");
            }
        });



    }

    public int defaultFieldColor() {
        return ContextCompat.getColor(this, R.color.md_black);
    }



    private CardStyle m_cardStyle;

    private FrameLayout m_fieldSettingsView;
    private FrameLayout m_fontView;
    private FrameLayout m_marginsView;

    // views
    private RecyclerView m_fullFieldListView;
    private FieldListAdapter m_fieldListAdapter;
    private LinearLayout m_cardstyleEditorCards;

    // navigation
    BottomNavigationView m_bottomNavigation;


    // field setting controls
    private Spinner m_fieldSpinner;
    private ArrayAdapter<CardField> m_fieldSpinnerAdapter;
    private ValueSlider m_field_relativesize;
    private CheckBox m_lineReturnCheckBox;
    private CheckBox m_htmlCheckBox;
    private Spinner m_fieldAlignmentSpinner;
    private ArrayAdapter<CharSequence> m_fieldAlignmentAdapter;
    private ValueSlider m_field_leftmargin;
    private ImageView m_fieldColorCircle;
    private SpectrumPalette m_field_colorPalette;
    private LinearLayout m_field_colorSelector;

    // text controls
    private TextView m_text_font_family;
    private Button m_text_font_lookup;
    private ValueSlider m_text_basesize;

    // margin controls

    private CardTemplateKey m_cardTemplateKey;
    private CardTemplate m_cardTemplate;
    private Card m_card;

    private CardField m_currentCardField = null;

}
