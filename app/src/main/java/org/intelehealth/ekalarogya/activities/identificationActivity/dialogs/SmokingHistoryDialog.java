package org.intelehealth.ekalarogya.activities.identificationActivity.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.identificationActivity.callback.SmokingHistoryCallback;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.AlcoholConsumptionHistory;
import org.intelehealth.ekalarogya.activities.identificationActivity.data_classes.SmokingHistory;
import org.intelehealth.ekalarogya.databinding.DialogSmokingHistoryBinding;

import java.util.concurrent.atomic.AtomicBoolean;

public class SmokingHistoryDialog extends DialogFragment {

    public static final String TAG = "SmokingHistoryDialog";
    private DialogSmokingHistoryBinding binding;
    private SmokingHistoryCallback callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (SmokingHistoryCallback) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        binding = DialogSmokingHistoryBinding.inflate(inflater);

        builder.setView(binding.getRoot())
                .setPositiveButton(R.string.ok, (dialog, which) -> {

                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.primary_text));
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.primary_text));

            positiveButton.setOnClickListener(v -> {
                if (!isDataValid())
                    Toast.makeText(requireContext(), "All fields are mandatory here", Toast.LENGTH_SHORT).show();
                else {
                    SmokingHistory smokingHistory = fetchData();
                    callback.saveSmokingHistory(smokingHistory);
                    dialog1.dismiss();
                }
            });
        });

        setListeners();
        return dialog;
    }

    private void setListeners() {
        binding.smokerRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.smokerRadioButton.getId())
                binding.smokerLinearLayout.setVisibility(View.VISIBLE);
            else
                binding.smokerLinearLayout.setVisibility(View.GONE);
        });
    }

    private Boolean isDataValid() {
        AtomicBoolean validation = new AtomicBoolean(true);

        if (binding.smokerRadioGroup.getCheckedRadioButtonId() == -1) {
            validation.set(false);
            return validation.get();
        }

        if (binding.smokerLinearLayout.getVisibility() == View.VISIBLE) {
            // If the person answers the above question as Yes, only then this linearlayout will be visible
            // Only in this case, will we be validating these RadioGroups

            if (binding.rateOfConsumptionRadioGroup.getCheckedRadioButtonId() == -1) {
                validation.set(false);
                return validation.get();
            }

            if (binding.durationOfSmokingRadioGroup.getCheckedRadioButtonId() == -1) {
                validation.set(false);
                return validation.get();
            }
        }

        return validation.get();
    }

    private SmokingHistory fetchData() {
        SmokingHistory smokingHistory = new SmokingHistory();
        // History of smoking
        smokingHistory.setSmokingStatus(
                ((RadioButton) binding.smokerRadioGroup.findViewById(binding.smokerRadioGroup.getCheckedRadioButtonId())).getText().toString()
        );

        if (binding.smokerLinearLayout.getVisibility() == View.VISIBLE) {
            // Rate of smoking
            smokingHistory.setRateOfSmoking(
                    ((RadioButton) binding.rateOfConsumptionRadioGroup.findViewById(binding.rateOfConsumptionRadioGroup.getCheckedRadioButtonId())).getText().toString()
            );
            // Duration of smoking
            smokingHistory.setDurationOfSmoking(
                    ((RadioButton) binding.durationOfSmokingRadioGroup.findViewById(binding.durationOfSmokingRadioGroup.getCheckedRadioButtonId())).getText().toString()
            );
        }

        return smokingHistory;
    }
}
