package mx.uv.comedor;

import android.os.Bundle;
import android.webkit.WebView;
import androidx.activity.OnBackPressedCallback;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getBridge() != null && getBridge().getWebView() != null) {
            getBridge().getWebView().clearCache(true);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                WebView webView = getBridge() != null ? getBridge().getWebView() : null;
                if (webView == null) {
                    moveTaskToBack(true);
                    return;
                }

                String script = "(function() {"
                    + "var btnCerrar = document.querySelector('[data-cerrar-modal-movil]');"
                    + "if (btnCerrar) { btnCerrar.click(); return 'modal'; }"
                    + "var hash = window.location.hash || '';"
                    + "var ruta = hash.startsWith('#') ? hash.slice(1) : window.location.pathname;"
                    + "if (ruta && ruta !== '/' && ruta !== '/login' && ruta !== '/personal' && window.history.length > 1) {"
                    + "window.history.back(); return 'historial';"
                    + "}"
                    + "return 'raiz';"
                    + "})();";

                webView.evaluateJavascript(script, resultado -> {
                    if ("\"raiz\"".equals(resultado)) {
                        moveTaskToBack(true);
                    }
                });
            }
        });
    }
}
