package com.celvansystems.projetoamigoanimal.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.helper.Constantes;
import com.celvansystems.projetoamigoanimal.helper.Util;

import java.util.Random;

public class DoacaoFragment extends Fragment implements BillingProcessor.IBillingHandler {

    private View view;
    private BillingProcessor bp;
    private View layout;
    private ImageView imvDoacao;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_doacao, container, false);
        inializaComponentes();
        carregarFotoMarketing();
        return view;
    }

    private void carregarFotoMarketing() {

        try {

            int aleatorio = aleatoriar(1, 3);

            for (int i = 1; i <= 3; i++) {

                if(aleatorio == i) {
                    if (i == 1) {
                        imvDoacao.setImageResource(R.drawable.marketing1);
                    } else if (i == 2) {
                        imvDoacao.setImageResource(R.drawable.marketing2);
                    } else {
                        imvDoacao.setImageResource(R.drawable.marketing3);
                    }
                } else {
                    imvDoacao.setImageResource(R.drawable.marketing1);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private int aleatoriar(int minimo, int maximo) {
        Random random = new Random();
        return random.nextInt((maximo - minimo) + 1) + minimo;
    }

    private void inializaComponentes() {

        bp = new BillingProcessor(view.getContext(), Constantes.LICENSE_KEY_GOOGLE_PLAY, this);

        bp.initialize();

        layout = view.findViewById(R.id.frame_layout_doacao);

        imvDoacao = view.findViewById(R.id.imv_doacao);

        Button btnDoar10 = view.findViewById(R.id.btn_doar10_reais);
        Button btnDoar5 = view.findViewById(R.id.btn_doar5_reais);
        Button btnDoar2 = view.findViewById(R.id.btn_doar2_reais);

        Button btnDoar50 = view.findViewById(R.id.btn_doar50_reais);
        Button btnDoar100 = view.findViewById(R.id.btn_doar100_reais);
        Button btnDoar500 = view.findViewById(R.id.btn_doar500_reais);

        btnDoar10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.consumePurchase(Constantes.PRODUCT_ID_10_REAIS);
                bp.purchase(getActivity(), Constantes.PRODUCT_ID_10_REAIS);
            }
        });
        btnDoar5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.consumePurchase(Constantes.PRODUCT_ID_5_REAIS);
                bp.purchase(getActivity(), Constantes.PRODUCT_ID_5_REAIS);
            }
        });
        btnDoar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.consumePurchase(Constantes.PRODUCT_ID_2_REAIS);
                bp.purchase(getActivity(), Constantes.PRODUCT_ID_2_REAIS);
            }
        });

        btnDoar50.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.consumePurchase(Constantes.PRODUCT_ID_50_REAIS);
                bp.purchase(getActivity(), Constantes.PRODUCT_ID_50_REAIS);
            }
        });
        btnDoar100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.consumePurchase(Constantes.PRODUCT_ID_100_REAIS);
                bp.purchase(getActivity(), Constantes.PRODUCT_ID_100_REAIS);
            }
        });
        btnDoar500.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.consumePurchase(Constantes.PRODUCT_ID_500_REAIS);
                bp.purchase(getActivity(), Constantes.PRODUCT_ID_500_REAIS);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btnDoar2.setElevation(10);
            btnDoar5.setElevation(10);
            btnDoar10.setElevation(10);
            CardView card = view.findViewById(R.id.cardView_doacao);
            card.setBackgroundTintList(view.getContext().getResources().getColorStateList(R.color.backgroundcolor));
        }
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        Util.setSnackBar(layout, getString(R.string.valor_indisponivel));
    }

    @Override
    public void onPurchaseHistoryRestored() {
        //Util.setSnackBar(layout, "History restored!");
    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        Util.setSnackBar(layout, getString(R.string.error) + errorCode);
    }

    @Override
    public void onBillingInitialized() {
        Util.setSnackBar(layout, getString(R.string.escolha_opcao_ajude_manter_app));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }



    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }


}
