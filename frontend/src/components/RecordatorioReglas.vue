<template>
  <section class="bg-white border border-slate-200 border-l-4 border-l-uv-azul rounded-xl p-4 shadow-sm space-y-2.5">
    <div class="flex items-center gap-2 text-uv-azul">
      <svg class="w-4 h-4 shrink-0" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
        <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a.75.75 0 000 1.5h.253a.25.25 0 01.244.304l-.459 2.066A1.75 1.75 0 0010.747 15H11a.75.75 0 000-1.5h-.253a.25.25 0 01-.244-.304l.459-2.066A1.75 1.75 0 009.253 9H9z" clip-rule="evenodd" />
      </svg>
      <h3 class="text-sm font-bold text-slate-900">Recuerda al hacer tu pedido</h3>
    </div>

    <ul class="space-y-1.5 text-xs text-slate-600 leading-relaxed list-disc pl-4">
      <li>
        Solo puedes tener <span class="font-medium text-slate-800">un pedido activo</span> a la vez. Si te equivocaste, puedes cancelarlo mientras siga en estado Recibido.
      </li>
      <li>
        Al pasar a <span class="font-medium text-slate-800">Listo para recoger</span>, cuentas con <span class="font-medium text-slate-800">15 minutos</span> para pasar al mostrador con tu codigo.
      </li>
      <li>
        Si no lo recoges a tiempo se suma una falta. Con <span class="font-medium text-slate-800">3 faltas</span> se desactiva el pago en mostrador por 1 mes (solo tarjeta en app).
      </li>
      <li>
        Si acumulas una <span class="font-medium text-slate-800">4ta falta</span> durante la restriccion, los pedidos en linea se suspenden 15 dias adicionales.
      </li>
      <li>
        Con 1 o 2 faltas, si pasan <span class="font-medium text-slate-800">60 dias</span> sin reincidir tu contador regresa a 0.
      </li>
    </ul>
  </section>
</template>
