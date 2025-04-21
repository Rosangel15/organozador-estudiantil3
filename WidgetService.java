public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetListFactory(this.getApplicationContext(), intent);
    }
}

class WidgetListFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private List<NotaWidget> notas = new ArrayList<>();

    public WidgetListFactory(Context context, Intent intent) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        cargarNotas();
    }

    @Override
    public void onDataSetChanged() {
        cargarNotas();
    }

    private void cargarNotas() {
        notas.clear();
        SharedPreferences prefs = context.getSharedPreferences("NotasPrefs", Context.MODE_PRIVATE);
        String notasJson = prefs.getString("notas", "[]");
        
        try {
            JSONArray notasArray = new JSONArray(notasJson);
            
            for (int i = 0; i < notasArray.length(); i++) {
                JSONObject notaJson = notasArray.getJSONObject(i);
                NotaWidget nota = new NotaWidget(
                    notaJson.getString("title"),
                    notaJson.getString("date"),
                    notaJson.optString("alarm", ""),
                    notaJson.getString("priority"),
                    notaJson.getString("content")
                );
                notas.add(nota);
            }
            
            // Ordenar por prioridad y luego por fecha/hora
            Collections.sort(notas, (n1, n2) -> {
                int prioridad1 = obtenerValorPrioridad(n1.prioridad);
                int prioridad2 = obtenerValorPrioridad(n2.prioridad);
                
                if (prioridad1 != prioridad2) {
                    return Integer.compare(prioridad1, prioridad2);
                }
                
                return n1.fecha.compareTo(n2.fecha);
            });
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private int obtenerValorPrioridad(String prioridad) {
        switch (prioridad) {
            case "urgent": return 0;
            case "high": return 1;
            case "medium": return 2;
            case "low": return 3;
            default: return 4;
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        NotaWidget nota = notas.get(position);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_item);
        
        // Configurar los datos de la nota
        views.setTextViewText(R.id.widget_item_title, nota.titulo);
        views.setTextViewText(R.id.widget_item_date, nota.fecha);
        
        // Mostrar solo un fragmento del contenido
        String contenidoCorto = nota.contenido.length() > 30 ? 
            nota.contenido.substring(0, 30) + "..." : nota.contenido;
        views.setTextViewText(R.id.widget_item_content, contenidoCorto);
        
        // Mostrar la alarma si existe
        if (!nota.alarma.isEmpty()) {
            views.setTextViewText(R.id.widget_item_alarm, "⏰ " + nota.alarma);
            views.setViewVisibility(R.id.widget_item_alarm, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_item_alarm, View.GONE);
        }
        
        // Color según prioridad
        int colorResId;
        switch (nota.prioridad) {
            case "urgent": colorResId = R.color.urgent; break;
            case "high": colorResId = R.color.high; break;
            case "medium": colorResId = R.color.medium; break;
            case "low": colorResId = R.color.low; break;
            default: colorResId = R.color.medium;
        }
        views.setInt(R.id.widget_item_root, "setBackgroundColor", 
            ContextCompat.getColor(context, colorResId));
        
        // Intent para abrir la nota en la app
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("nota_id", position);
        views.setOnClickFillInIntent(R.id.widget_item_root, fillInIntent);
        
        return views;
    }

    // ... otros métodos requeridos por la interfaz
}

class NotaWidget {
    String titulo;
    String fecha;
    String alarma;
    String prioridad;
    String contenido;
    
    public NotaWidget(String titulo, String fecha, String alarma, String prioridad, String contenido) {
        this.titulo = titulo;
        this.fecha = fecha;
        this.alarma = alarma;
        this.prioridad = prioridad;
        this.contenido = contenido;
    }
}