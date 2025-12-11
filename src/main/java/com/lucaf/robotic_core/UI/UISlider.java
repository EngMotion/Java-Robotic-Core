package com.lucaf.robotic_core.UI;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class UISlider {
    @Getter
    final JPanel sliderTemplate = new JPanel();
    final JLabel label = new JLabel();
    final JSlider slider = new JSlider();
    final JSpinner spinner = new JSpinner();
    @Setter
    Consumer<Integer> onValueChange;

    public UISlider(String label, int min, int max, int currentValue) {
        sliderTemplate.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        sliderTemplate.add(this.label, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sliderTemplate.add(this.slider, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sliderTemplate.add(this.spinner, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(70, -1), null, null, 0, false));
        setText(label);
        setMin(min);
        setMax(max);
        setValue(currentValue);
        this.spinner.addChangeListener(e -> {
            int value = (int) this.spinner.getValue();
            this.slider.setValue(value);
            if (onValueChange != null) {
                onValueChange.accept(value);
            }
        });
        this.slider.addChangeListener(e -> {
            int value = this.slider.getValue();
            this.spinner.setValue(value);
            if (onValueChange != null) {
                onValueChange.accept(value);
            }
        });
    }

    public UISlider(String label, int min, int max) {
        this(label, min, max, min);
    }

    public UISlider(String label) {
        this(label, 0, 100, 0);
    }

    public void setText(String text) {
        label.setText(text);
    }

    public void setMin(int min) {
        slider.setMinimum(min);
    }

    public void setMax(int max) {
        slider.setMaximum(max);
    }

    public void setValue(int value) {
        slider.setValue(value);
        spinner.setValue(value);
    }

}
