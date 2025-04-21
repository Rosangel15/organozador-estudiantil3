public class MiWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Actualizar todos los widgets
        for (int appWidgetId : appWidgetIds) {
            actualizarWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void actualizarWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Obtener las notas desde SharedPreferences (que sincronizarás con tu web)
        SharedPreferences prefs = context.getSharedPreferences("NotasPrefs", Context.MODE_PRIVATE);
        String notasJson = prefs.getString("notas", "[]");
        
        try {
            JSONArray notasArray = new JSONArray(notasJson);
            
            // Crear RemoteViews para el diseño del widget
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            
            // Configurar ListView del widget
            Intent intent = new Intent(context, WidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            
            views.setRemoteAdapter(R.id.widget_list, intent);
            views.setEmptyView(R.id.widget_list, R.id.widget_empty);
            
            // Configurar intent para abrir la app al hacer clic
            Intent appIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_header, pendingIntent);
            
            // Notificar al AppWidgetManager para actualizar el widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list);
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}