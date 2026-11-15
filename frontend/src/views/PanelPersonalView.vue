<template>
  <div class="max-w-6xl mx-auto px-4 py-6 space-y-6">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div class="bg-white border border-slate-200 rounded-xl p-1 grid grid-cols-3 sm:inline-flex gap-1 text-xs sm:text-sm shadow-sm w-full sm:w-auto">
        <button
          type="button"
          @click="pestana = 'cola'"
          class="px-2.5 sm:px-4 py-2 rounded-lg font-medium transition-colors text-center"
          :class="pestana === 'cola' ? 'bg-uv-azul text-white' : 'text-slate-600 hover:bg-slate-100'"
        >
          <span class="sm:hidden">Cola ({{ cola.length }})</span>
          <span class="hidden sm:inline">Cola de pedidos ({{ cola.length }})</span>
        </button>
        <button
          type="button"
          @click="pestana = 'menu'"
          class="px-2.5 sm:px-4 py-2 rounded-lg font-medium transition-colors text-center"
          :class="pestana === 'menu' ? 'bg-uv-azul text-white' : 'text-slate-600 hover:bg-slate-100'"
        >
          <span class="sm:hidden">Menú</span>
          <span class="hidden sm:inline">Menú y disponibilidad</span>
        </button>
        <button
          type="button"
          @click="abrirHistorico"
          class="px-2.5 sm:px-4 py-2 rounded-lg font-medium transition-colors text-center"
          :class="pestana === 'historico' ? 'bg-uv-azul text-white' : 'text-slate-600 hover:bg-slate-100'"
        >
          <span class="sm:hidden">Histórico</span>
          <span class="hidden sm:inline">Histórico de pedidos</span>
        </button>
      </div>

      <button
        type="button"
        @click="alternarRecepcion"
        class="w-full sm:w-auto px-3.5 py-2 rounded-xl border text-xs font-semibold shadow-sm transition-colors"
        :class="
          catalogo.recepcionAbierta
            ? 'bg-uv-verde-claro border-uv-verde/40 text-uv-verde-oscuro hover:bg-amber-50 hover:border-amber-300 hover:text-amber-900'
            : 'bg-amber-50 border-amber-300 text-amber-900 hover:bg-uv-verde-claro hover:border-uv-verde/40 hover:text-uv-verde-oscuro'
        "
      >
        Recepción en línea: {{ catalogo.recepcionAbierta ? 'Abierta' : 'Cerrada' }}
      </button>
    </div>

    <div v-if="mensajeOperacion" class="rounded-xl border border-uv-verde/40 bg-uv-verde-claro px-4 py-2.5 text-sm text-uv-verde-oscuro shadow-sm flex items-center justify-between">
      <span>{{ mensajeOperacion }}</span>
      <button type="button" @click="mensajeOperacion = ''" class="text-xs font-semibold underline">Cerrar</button>
    </div>

    <div v-if="errorOperacion" class="rounded-xl border border-red-200 bg-red-50 px-4 py-2.5 text-sm text-red-800 shadow-sm flex items-center justify-between">
      <span>{{ errorOperacion }}</span>
      <button type="button" @click="errorOperacion = ''" class="text-xs font-semibold underline">Cerrar</button>
    </div>

    <div v-if="pestana === 'cola'" class="space-y-6">
      <section class="bg-white border border-slate-200 rounded-xl p-4 shadow-sm space-y-3">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 class="text-sm font-bold text-slate-900">Validación rápida en mostrador</h2>
            <p class="text-xs text-slate-500">Ingresa el código de referencia del estudiante para validar su cobro y entrega</p>
          </div>
          <form @submit.prevent="buscarReferencia" class="flex flex-wrap items-center gap-2 w-full sm:w-auto">
            <input
              v-model="referenciaBusqueda"
              type="text"
              maxlength="4"
              placeholder="Ej. A001"
              class="flex-1 sm:flex-none sm:w-32 min-w-[110px] uppercase rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-semibold tracking-wider focus:outline-none focus:ring-2 focus:ring-uv-azul/20 focus:border-uv-azul"
            />
            <button
              type="submit"
              class="px-3.5 py-1.5 rounded-lg bg-uv-azul hover:bg-uv-azul-oscuro text-white text-sm font-medium shrink-0"
            >
              Buscar
            </button>
            <button
              v-if="pedidoEncontrado"
              type="button"
              @click="limpiarBusqueda"
              class="px-2.5 py-1.5 rounded-lg border border-slate-300 text-xs text-slate-600 hover:bg-slate-50 shrink-0"
            >
              Limpiar
            </button>
          </form>
        </div>

        <div v-if="pedidoEncontrado" class="rounded-xl border border-uv-azul/30 bg-uv-azul-claro/40 p-4 flex flex-wrap items-center justify-between gap-4">
          <div class="space-y-1 text-sm">
            <div class="flex flex-wrap items-center gap-2">
              <span class="text-xl font-bold text-uv-azul tabular-nums">{{ pedidoEncontrado.referencia }}</span>
              <span class="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-white border border-slate-200 text-slate-800">
                {{ etiquetaEstadoPedido(pedidoEncontrado.estadoPedido) }}
              </span>
              <span
                class="px-2.5 py-0.5 rounded-full text-xs font-semibold"
                :class="pedidoEncontrado.modalidadPago === 'mostrador' ? 'bg-amber-100 text-amber-900' : 'bg-uv-verde-claro text-uv-verde-oscuro'"
              >
                {{ pedidoEncontrado.modalidadPago === 'mostrador' ? 'Cobrar en caja: ' + formatearMoneda(pedidoEncontrado.total) : 'App tarjeta (' + etiquetaEstadoPago(pedidoEncontrado.estadoPago) + ')' }}
              </span>
            </div>
            <p class="text-slate-700">
              Estudiante: <span class="font-medium">{{ pedidoEncontrado.nombreEstudiante }}</span> ({{ pedidoEncontrado.credencialEstudiante }})
            </p>
            <p class="text-xs text-slate-600">
              {{ pedidoEncontrado.detalles.map((d) => `${d.cantidad}x ${d.nombrePlatillo}`).join(' · ') }}
            </p>
          </div>

          <div v-if="siguienteEstado(pedidoEncontrado.estadoPedido)">
            <button
              type="button"
              @click="solicitarAvanceEstado(pedidoEncontrado)"
              class="px-4 py-2 rounded-lg text-sm font-semibold text-white"
              :class="pedidoEncontrado.estadoPedido === 'listo_para_recoger' ? 'bg-uv-verde hover:bg-uv-verde-oscuro' : 'bg-uv-azul hover:bg-uv-azul-oscuro'"
            >
              {{ textoAccionEstado(pedidoEncontrado.estadoPedido) }}
            </button>
          </div>
        </div>
      </section>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <div
          v-for="columna in columnasCola"
          :key="columna.estado"
          class="bg-white border border-slate-200 border-t-4 rounded-xl shadow-sm flex flex-col overflow-hidden"
          :class="columna.bordeSuperior"
        >
          <div class="px-4 py-3 border-b border-slate-200 flex items-center justify-between bg-slate-50/70">
            <h3 class="text-sm font-bold text-slate-800">{{ columna.titulo }}</h3>
            <span class="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-white border border-slate-200 text-slate-700 tabular-nums">
              {{ columna.items.length }}
            </span>
          </div>

          <div class="p-3 space-y-3 flex-1">
            <p v-if="columna.items.length === 0" class="text-xs text-slate-400 text-center py-8">
              Sin pedidos en esta etapa
            </p>

            <article
              v-for="pedido in columna.items"
              :key="pedido.id"
              class="border border-slate-200 rounded-lg p-3.5 space-y-2.5 bg-white"
            >
              <div class="flex items-start justify-between gap-2">
                <div>
                  <span class="text-xl font-bold text-uv-azul tabular-nums">{{ pedido.referencia }}</span>
                  <span class="ml-2 text-xs text-slate-500">{{ etiquetaTurno(pedido.turno) }}</span>
                </div>
                <span class="text-sm font-bold text-slate-900 tabular-nums">{{ formatearMoneda(pedido.total) }}</span>
              </div>

              <div class="text-xs text-slate-600">
                <p class="font-medium text-slate-800">{{ pedido.nombreEstudiante }}</p>
                <p>{{ pedido.credencialEstudiante }} · {{ formatearHora(pedido.fechaCreacion) }}</p>
              </div>

              <ul class="text-xs text-slate-700 bg-slate-50 rounded-md p-2.5 space-y-1">
                <li v-for="d in pedido.detalles" :key="d.id" class="flex justify-between gap-2">
                  <span>{{ d.cantidad }} x {{ d.nombrePlatillo }}</span>
                  <span class="tabular-nums text-slate-500">{{ formatearMoneda(d.subtotal) }}</span>
                </li>
              </ul>

              <div class="flex items-center justify-between gap-2 text-xs">
                <span
                  class="px-2 py-0.5 rounded font-medium"
                  :class="pedido.modalidadPago === 'mostrador' ? 'bg-amber-50 text-amber-800 border border-amber-200' : 'bg-uv-azul-claro text-uv-azul'"
                >
                  {{ pedido.modalidadPago === 'mostrador' ? 'Cobrar en mostrador' : 'Pagado en app (retenido)' }}
                </span>

                <span
                  v-if="pedido.estadoPedido === 'listo_para_recoger'"
                  class="font-semibold text-uv-verde-oscuro tabular-nums"
                >
                  Vence {{ formatearRestante(pedido.fechaLimiteRecoleccion) }}
                </span>
              </div>

              <div class="space-y-1.5">
                <button
                  type="button"
                  @click="solicitarAvanceEstado(pedido)"
                  class="w-full py-2 rounded-md text-xs font-semibold text-white transition-colors"
                  :class="pedido.estadoPedido === 'listo_para_recoger' ? 'bg-uv-verde hover:bg-uv-verde-oscuro' : 'bg-uv-azul hover:bg-uv-azul-oscuro'"
                >
                  {{ textoAccionEstado(pedido.estadoPedido) }}
                </button>

                <button
                  v-if="pedido.estadoPedido === 'listo_para_recoger'"
                  type="button"
                  @click="pedidoAVencer = pedido"
                  class="w-full py-1.5 rounded-md text-[11px] font-medium border border-slate-200 text-slate-600 hover:bg-red-50 hover:border-red-200 hover:text-red-700 transition-colors"
                >
                  Marcar no reclamado
                </button>
              </div>
            </article>
          </div>
        </div>
      </div>
    </div>

    <div v-if="pestana === 'menu'" class="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
      <div class="lg:col-span-2 space-y-6">
        <section class="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
          <div class="px-5 py-4 border-b border-slate-200">
            <h2 class="text-base font-bold text-slate-900">Comida del día ({{ catalogo.fechaOperativa }})</h2>
            <p class="text-xs text-slate-500">Apaga la disponibilidad en cuanto se termine para bloquear nuevos pedidos.</p>
          </div>

          <div v-if="catalogo.comidasDelDia.length === 0" class="p-5 text-sm text-slate-500">
            Aún no hay comidas del día publicadas hoy.
          </div>

          <ul v-else class="divide-y divide-slate-100">
            <li
              v-for="platillo in catalogo.comidasDelDia"
              :key="platillo.id"
              class="p-4 flex flex-wrap items-center justify-between gap-3"
            >
              <div>
                <div class="flex items-center gap-2">
                  <span class="font-semibold text-slate-900">{{ platillo.nombre }}</span>
                  <span class="px-2 py-0.5 rounded-full text-xs bg-slate-100 text-slate-700">
                    {{ etiquetaTurno(platillo.turno) }}
                  </span>
                </div>
                <p v-if="platillo.descripcion" class="text-xs text-slate-500 mt-0.5">{{ platillo.descripcion }}</p>
                <p class="text-xs font-semibold text-uv-azul mt-1 tabular-nums">{{ formatearMoneda(platillo.precio) }}</p>
              </div>

              <button
                type="button"
                @click="alternarDisponibilidad(platillo)"
                class="px-3.5 py-1.5 rounded-md text-xs font-semibold border transition-colors"
                :class="
                  platillo.disponible
                    ? 'bg-uv-verde-claro border-uv-verde/40 text-uv-verde-oscuro hover:bg-red-50 hover:border-red-200 hover:text-red-700'
                    : 'bg-red-50 border-red-200 text-red-700 hover:bg-uv-verde-claro hover:border-uv-verde/40 hover:text-uv-verde-oscuro'
                "
              >
                {{ platillo.disponible ? 'Disponible (marcar agotado)' : 'Agotado (habilitar)' }}
              </button>
            </li>
          </ul>
        </section>

        <section class="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
          <div class="px-5 py-4 border-b border-slate-200">
            <h2 class="text-base font-bold text-slate-900">Menú</h2>
            <p class="text-xs text-slate-500">Los platillos no se eliminan para conservar el histórico de ventas.</p>
          </div>

          <ul class="divide-y divide-slate-100">
            <li
              v-for="platillo in catalogo.menuRegular"
              :key="platillo.id"
              class="p-4 flex flex-wrap items-center justify-between gap-3"
            >
              <div class="min-w-0 flex-1">
                <div class="flex items-center gap-2">
                  <span class="font-semibold text-slate-900">{{ platillo.nombre }}</span>
                  <span class="px-2 py-0.5 rounded-full text-xs bg-slate-100 text-slate-700">
                    {{ etiquetaTurno(platillo.turno) }}
                  </span>
                </div>
                <p v-if="platillo.descripcion" class="text-xs text-slate-500 mt-0.5">{{ platillo.descripcion }}</p>
                <p class="text-xs font-semibold text-uv-azul mt-1 tabular-nums">{{ formatearMoneda(platillo.precio) }}</p>
              </div>

              <div class="flex items-center gap-2 shrink-0">
                <button
                  type="button"
                  @click="abrirEdicionPlatillo(platillo)"
                  class="px-3 py-1.5 rounded-md text-xs font-medium border border-slate-300 text-slate-700 hover:bg-slate-50"
                >
                  Editar
                </button>
                <button
                  type="button"
                  @click="alternarDisponibilidad(platillo)"
                  class="px-3.5 py-1.5 rounded-md text-xs font-semibold border transition-colors"
                  :class="
                    platillo.disponible
                      ? 'bg-uv-verde-claro border-uv-verde/40 text-uv-verde-oscuro hover:bg-red-50 hover:border-red-200 hover:text-red-700'
                      : 'bg-red-50 border-red-200 text-red-700 hover:bg-uv-verde-claro hover:border-uv-verde/40 hover:text-uv-verde-oscuro'
                  "
                >
                  {{ platillo.disponible ? 'Disponible' : 'Agotado' }}
                </button>
              </div>
            </li>
          </ul>
        </section>
      </div>

      <div class="space-y-6">
        <section class="bg-white border border-slate-200 rounded-xl p-5 shadow-sm">
          <h3 class="text-sm font-bold text-slate-900 mb-3">Publicar comida del día</h3>
          <form @submit.prevent="publicarComidaDelDia" class="space-y-3 text-sm">
            <div>
              <label class="block text-xs font-medium text-slate-700 mb-1">Turno</label>
              <select
                v-model="formComidaDia.turno"
                class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm bg-white focus:outline-none focus:border-uv-azul"
              >
                <option value="matutino">Matutino</option>
                <option value="vespertino">Vespertino</option>
              </select>
            </div>
            <div>
              <label class="block text-xs font-medium text-slate-700 mb-1">Nombre del platillo</label>
              <input
                v-model="formComidaDia.nombre"
                type="text"
                required
                placeholder="Ej. Milanesa de pollo con ensalada"
                class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:border-uv-azul"
              />
            </div>
            <div>
              <label class="block text-xs font-medium text-slate-700 mb-1">Complementos y agua</label>
              <textarea
                v-model="formComidaDia.descripcion"
                rows="2"
                placeholder="Arroz, frijoles y agua del dia"
                class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:border-uv-azul"
              ></textarea>
            </div>
            <div>
              <label class="block text-xs font-medium text-slate-700 mb-1">Precio fijo (MXN)</label>
              <input
                v-model="formComidaDia.precio"
                type="number"
                step="0.50"
                min="1"
                required
                class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:border-uv-azul"
              />
            </div>
            <button
              type="submit"
              class="w-full py-2.5 rounded-lg bg-uv-verde hover:bg-uv-verde-oscuro text-white text-sm font-semibold"
            >
              Publicar para hoy
            </button>
          </form>
        </section>

        <section class="bg-white border border-slate-200 rounded-xl p-5 shadow-sm">
          <h3 class="text-sm font-bold text-slate-900 mb-3">Agregar al menú</h3>
          <form @submit.prevent="agregarPlatilloRegular" class="space-y-3 text-sm">
            <div>
              <label class="block text-xs font-medium text-slate-700 mb-1">Nombre</label>
              <input
                v-model="formRegular.nombre"
                type="text"
                required
                placeholder="Ej. Sincronizadas"
                class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:border-uv-azul"
              />
            </div>
            <div>
              <label class="block text-xs font-medium text-slate-700 mb-1">Descripción</label>
              <textarea
                v-model="formRegular.descripcion"
                rows="2"
                placeholder="Ingredientes o porción"
                class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:border-uv-azul"
              ></textarea>
            </div>
            <div class="grid grid-cols-2 gap-2">
              <div>
                <label class="block text-xs font-medium text-slate-700 mb-1">Precio</label>
                <input
                  v-model="formRegular.precio"
                  type="number"
                  step="0.50"
                  min="1"
                  required
                  class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:border-uv-azul"
                />
              </div>
              <div>
                <label class="block text-xs font-medium text-slate-700 mb-1">Turno</label>
                <select
                  v-model="formRegular.turno"
                  class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm bg-white focus:outline-none focus:border-uv-azul"
                >
                  <option value="ambos">Ambos</option>
                  <option value="matutino">Matutino</option>
                  <option value="vespertino">Vespertino</option>
                </select>
              </div>
            </div>
            <button
              type="submit"
              class="w-full py-2.5 rounded-lg bg-uv-azul hover:bg-uv-azul-oscuro text-white text-sm font-semibold"
            >
              Guardar platillo
            </button>
          </form>
        </section>
      </div>
    </div>

    <div v-if="pestana === 'historico'" class="space-y-5">
      <section class="bg-white border border-slate-200 rounded-xl p-4 shadow-sm grid grid-cols-1 sm:grid-cols-2 lg:flex lg:flex-wrap items-end gap-3 text-sm">
        <div>
          <label class="block text-xs font-medium text-slate-600 mb-1">Fecha operativa</label>
          <div class="flex items-center gap-1.5">
            <input
              v-model="filtrosHistorico.fecha"
              type="date"
              class="w-full lg:w-auto rounded-lg border border-slate-300 px-2.5 py-1.5 text-sm focus:outline-none focus:border-uv-azul"
            />
            <button
              type="button"
              @click="filtrosHistorico.fecha = ''; cargarHistorico()"
              class="px-2.5 py-1.5 rounded-lg border border-slate-300 text-xs text-slate-600 hover:bg-slate-50 shrink-0"
            >
              Todas
            </button>
          </div>
        </div>

        <div>
          <label class="block text-xs font-medium text-slate-600 mb-1">Turno</label>
          <select
            v-model="filtrosHistorico.turno"
            class="w-full lg:w-auto rounded-lg border border-slate-300 px-3 py-1.5 text-sm bg-white focus:outline-none focus:border-uv-azul"
          >
            <option value="">Todos</option>
            <option value="matutino">Matutino</option>
            <option value="vespertino">Vespertino</option>
          </select>
        </div>

        <div>
          <label class="block text-xs font-medium text-slate-600 mb-1">Modalidad de pago</label>
          <select
            v-model="filtrosHistorico.modalidadPago"
            class="w-full lg:w-auto rounded-lg border border-slate-300 px-3 py-1.5 text-sm bg-white focus:outline-none focus:border-uv-azul"
          >
            <option value="">Todas</option>
            <option value="app_tarjeta">Tarjeta en app</option>
            <option value="mostrador">En mostrador</option>
          </select>
        </div>

        <div>
          <label class="block text-xs font-medium text-slate-600 mb-1">Estado</label>
          <select
            v-model="filtrosHistorico.estado"
            class="w-full lg:w-auto rounded-lg border border-slate-300 px-3 py-1.5 text-sm bg-white focus:outline-none focus:border-uv-azul"
          >
            <option value="">Todos</option>
            <option value="entregado">Entregados</option>
            <option value="no_reclamado">No reclamados</option>
          </select>
        </div>

        <button
          type="button"
          @click="cargarHistorico"
          class="sm:col-span-2 lg:col-span-1 w-full lg:w-auto px-4 py-1.5 rounded-lg bg-uv-azul hover:bg-uv-azul-oscuro text-white text-sm font-medium"
        >
          Aplicar filtros
        </button>
      </section>

      <div class="grid grid-cols-2 lg:grid-cols-4 gap-3">
        <div class="bg-white border border-slate-200 border-l-4 border-l-uv-verde rounded-xl p-4 shadow-sm">
          <p class="text-xs text-slate-500">Entregados</p>
          <p class="text-2xl font-bold text-uv-verde-oscuro tabular-nums mt-1">{{ historico.totalEntregados }}</p>
        </div>
        <div class="bg-white border border-slate-200 border-l-4 border-l-red-500 rounded-xl p-4 shadow-sm">
          <p class="text-xs text-slate-500">No reclamados</p>
          <p class="text-2xl font-bold text-red-700 tabular-nums mt-1">{{ historico.totalNoReclamados }}</p>
        </div>
        <div class="bg-white border border-slate-200 border-l-4 border-l-uv-azul rounded-xl p-4 shadow-sm">
          <p class="text-xs text-slate-500">Cobrado en App</p>
          <p class="text-xl font-bold text-slate-900 tabular-nums mt-1">{{ formatearMoneda(historico.montoCobradoApp) }}</p>
        </div>
        <div class="bg-white border border-slate-200 border-l-4 border-l-amber-500 rounded-xl p-4 shadow-sm">
          <p class="text-xs text-slate-500">Cobrado en Mostrador</p>
          <p class="text-xl font-bold text-slate-900 tabular-nums mt-1">{{ formatearMoneda(historico.montoCobradoMostrador) }}</p>
        </div>
      </div>

      <section class="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
        <div v-if="historico.pedidos.length === 0" class="p-6 text-sm text-slate-500 text-center">
          No hay pedidos cerrados con los filtros seleccionados.
        </div>

        <template v-else>
          <div class="md:hidden divide-y divide-slate-100">
            <article v-for="p in historico.pedidos" :key="`hm-${p.id}`" class="p-4 space-y-2">
              <div class="flex items-start justify-between gap-2">
                <div>
                  <span class="text-base font-bold text-uv-azul tabular-nums">{{ p.referencia }}</span>
                  <span class="ml-2 text-xs text-slate-500">{{ p.fechaOperativa }} · {{ etiquetaTurno(p.turno) }}</span>
                </div>
                <span
                  class="px-2.5 py-0.5 rounded-full text-xs font-medium shrink-0"
                  :class="p.estadoPedido === 'entregado' ? 'bg-uv-verde-claro text-uv-verde-oscuro' : 'bg-red-50 text-red-700'"
                >
                  {{ etiquetaEstadoPedido(p.estadoPedido) }}
                </span>
              </div>

              <p class="text-xs text-slate-700">
                <span class="font-medium">{{ p.nombreEstudiante }}</span> ({{ p.credencialEstudiante }})
              </p>

              <p class="text-xs text-slate-600 bg-slate-50 rounded-md p-2">
                {{ p.detalles.map((d) => `${d.cantidad}x ${d.nombrePlatillo}`).join(', ') }}
              </p>

              <div class="flex items-center justify-between gap-2 text-xs">
                <span class="text-slate-500">
                  {{ etiquetaModalidadPago(p.modalidadPago) }} · {{ etiquetaEstadoPago(p.estadoPago) }}
                </span>
                <span class="text-sm font-bold text-slate-900 tabular-nums">{{ formatearMoneda(p.total) }}</span>
              </div>
            </article>
          </div>

          <div class="hidden md:block overflow-x-auto">
            <table class="w-full text-sm text-left">
              <thead class="bg-slate-50 text-xs uppercase text-slate-500 border-b border-slate-200">
                <tr>
                  <th class="px-4 py-3">Ref.</th>
                  <th class="px-4 py-3">Fecha y turno</th>
                  <th class="px-4 py-3">Estudiante</th>
                  <th class="px-4 py-3">Detalle</th>
                  <th class="px-4 py-3">Pago</th>
                  <th class="px-4 py-3">Estado</th>
                  <th class="px-4 py-3 text-right">Total</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-for="p in historico.pedidos" :key="p.id" class="hover:bg-slate-50">
                  <td class="px-4 py-3 font-bold text-uv-azul tabular-nums">{{ p.referencia }}</td>
                  <td class="px-4 py-3 text-xs text-slate-600">
                    <div>{{ p.fechaOperativa }}</div>
                    <div class="text-slate-400">{{ etiquetaTurno(p.turno) }}</div>
                  </td>
                  <td class="px-4 py-3 text-xs">
                    <div class="font-medium text-slate-800">{{ p.nombreEstudiante }}</div>
                    <div class="text-slate-400">{{ p.credencialEstudiante }}</div>
                  </td>
                  <td class="px-4 py-3 text-xs text-slate-700">
                    {{ p.detalles.map((d) => `${d.cantidad}x ${d.nombrePlatillo}`).join(', ') }}
                  </td>
                  <td class="px-4 py-3 text-xs text-slate-600">
                    <div>{{ etiquetaModalidadPago(p.modalidadPago) }}</div>
                    <div class="text-slate-400">{{ etiquetaEstadoPago(p.estadoPago) }}</div>
                  </td>
                  <td class="px-4 py-3">
                    <span
                      class="px-2.5 py-0.5 rounded-full text-xs font-medium"
                      :class="p.estadoPedido === 'entregado' ? 'bg-uv-verde-claro text-uv-verde-oscuro' : 'bg-red-50 text-red-700'"
                    >
                      {{ etiquetaEstadoPedido(p.estadoPedido) }}
                    </span>
                  </td>
                  <td class="px-4 py-3 text-right font-semibold tabular-nums">{{ formatearMoneda(p.total) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>
      </section>
    </div>

    <div
      v-if="pedidoAEntregar"
      class="fixed inset-0 bg-slate-900/40 z-50 flex items-center justify-center p-4"
      @click.self="pedidoAEntregar = null"
    >
      <div class="bg-white border border-slate-200 rounded-xl shadow-lg max-w-md w-full p-5 space-y-4 max-h-[90vh] overflow-y-auto">
        <div class="flex items-start justify-between gap-2">
          <div>
            <p class="text-xs uppercase tracking-wide text-slate-500 font-medium">Confirmar entrega</p>
            <h3 class="text-2xl font-bold text-uv-azul tabular-nums">{{ pedidoAEntregar.referencia }}</h3>
          </div>
          <span class="text-lg font-bold text-slate-900 tabular-nums">{{ formatearMoneda(pedidoAEntregar.total) }}</span>
        </div>

        <p class="text-sm text-slate-700">
          Estudiante: <span class="font-semibold">{{ pedidoAEntregar.nombreEstudiante }}</span> ({{ pedidoAEntregar.credencialEstudiante }})
        </p>

        <ul class="text-xs text-slate-700 bg-slate-50 border border-slate-200 rounded-lg p-3 space-y-1">
          <li v-for="d in pedidoAEntregar.detalles" :key="d.id" class="flex justify-between">
            <span>{{ d.cantidad }} x {{ d.nombrePlatillo }}</span>
            <span class="tabular-nums font-medium">{{ formatearMoneda(d.subtotal) }}</span>
          </li>
        </ul>

        <div
          class="rounded-lg border p-3 text-sm"
          :class="pedidoAEntregar.modalidadPago === 'mostrador' ? 'bg-amber-50 border-amber-300 text-amber-900' : 'bg-uv-verde-claro border-uv-verde/40 text-uv-verde-oscuro'"
        >
          <p v-if="pedidoAEntregar.modalidadPago === 'mostrador'" class="font-semibold">
            Cobrar {{ formatearMoneda(pedidoAEntregar.total) }} en caja antes de entregar (efectivo o tarjeta física).
          </p>
          <p v-else class="font-semibold">
            Pago retenido en app por {{ formatearMoneda(pedidoAEntregar.total) }}. Al confirmar se ejecutará el cobro definitivo.
          </p>
        </div>

        <div class="flex justify-end gap-2 pt-2">
          <button
            type="button"
            data-cerrar-modal-movil
            @click="pedidoAEntregar = null"
            class="px-4 py-2 rounded-lg border border-slate-300 text-sm text-slate-700 hover:bg-slate-50"
          >
            Cancelar
          </button>
          <button
            type="button"
            @click="confirmarEntrega"
            class="px-4 py-2 rounded-lg bg-uv-verde hover:bg-uv-verde-oscuro text-white text-sm font-semibold"
          >
            Confirmar cobro y entrega
          </button>
        </div>
      </div>
    </div>

    <div
      v-if="pedidoAVencer"
      class="fixed inset-0 bg-slate-900/40 z-50 flex items-center justify-center p-4"
      @click.self="pedidoAVencer = null"
    >
      <div class="bg-white border border-slate-200 rounded-xl shadow-lg max-w-md w-full p-5 space-y-4 max-h-[90vh] overflow-y-auto">
        <h3 class="text-base font-bold text-slate-900">
          Marcar pedido {{ pedidoAVencer.referencia }} como no reclamado
        </h3>
        <p class="text-sm text-slate-600 leading-relaxed">
          Esta acción cerrará el pedido de {{ pedidoAVencer.nombreEstudiante }}, liberará o cancelará el pago correspondiente y sumará 1 falta de abandono a su cuenta.
        </p>
        <div class="flex justify-end gap-2">
          <button
            type="button"
            data-cerrar-modal-movil
            @click="pedidoAVencer = null"
            class="px-4 py-2 rounded-lg border border-slate-300 text-sm text-slate-700 hover:bg-slate-50"
          >
            Cancelar
          </button>
          <button
            type="button"
            @click="confirmarVencimiento"
            class="px-4 py-2 rounded-lg bg-red-600 hover:bg-red-700 text-white text-sm font-semibold"
          >
            Confirmar no reclamado
          </button>
        </div>
      </div>
    </div>

    <div
      v-if="platilloEnEdicion"
      class="fixed inset-0 bg-slate-900/40 z-50 flex items-center justify-center p-4"
      @click.self="platilloEnEdicion = null"
    >
      <div class="bg-white border border-slate-200 rounded-xl shadow-lg max-w-md w-full p-5 space-y-4 max-h-[90vh] overflow-y-auto">
        <h3 class="text-base font-bold text-slate-900">Editar platillo del menú</h3>
        <form @submit.prevent="guardarEdicionPlatillo" class="space-y-3 text-sm">
          <div>
            <label class="block text-xs font-medium text-slate-700 mb-1">Nombre</label>
            <input
              v-model="platilloEnEdicion.nombre"
              type="text"
              required
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:border-uv-azul"
            />
          </div>
          <div>
            <label class="block text-xs font-medium text-slate-700 mb-1">Descripción</label>
            <textarea
              v-model="platilloEnEdicion.descripcion"
              rows="2"
              class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:border-uv-azul"
            ></textarea>
          </div>
          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="block text-xs font-medium text-slate-700 mb-1">Precio (MXN)</label>
              <input
                v-model="platilloEnEdicion.precio"
                type="number"
                step="0.50"
                min="1"
                required
                class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:border-uv-azul"
              />
            </div>
            <div>
              <label class="block text-xs font-medium text-slate-700 mb-1">Turno</label>
              <select
                v-model="platilloEnEdicion.turno"
                class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm bg-white focus:outline-none focus:border-uv-azul"
              >
                <option value="ambos">Ambos</option>
                <option value="matutino">Matutino</option>
                <option value="vespertino">Vespertino</option>
              </select>
            </div>
          </div>
          <div class="flex justify-end gap-2 pt-2">
            <button
              type="button"
              data-cerrar-modal-movil
              @click="platilloEnEdicion = null"
              class="px-4 py-2 rounded-lg border border-slate-300 text-sm text-slate-700 hover:bg-slate-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              class="px-4 py-2 rounded-lg bg-uv-azul hover:bg-uv-azul-oscuro text-white text-sm font-semibold"
            >
              Guardar cambios
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import api, { extraerMensajeError } from '../services/api'
import { suscribirEvento, socketConectado } from '../services/socket'
import {
  etiquetaEstadoPago,
  etiquetaEstadoPedido,
  etiquetaModalidadPago,
  etiquetaTurno,
  formatearHora,
  formatearMoneda
} from '../utils/formatos'

const pestana = ref('cola')
const cola = ref([])
const catalogo = ref({
  fechaOperativa: '',
  recepcionAbierta: true,
  comidasDelDia: [],
  menuRegular: []
})

const referenciaBusqueda = ref('')
const pedidoEncontrado = ref(null)
const pedidoAEntregar = ref(null)
const pedidoAVencer = ref(null)
const platilloEnEdicion = ref(null)

const mensajeOperacion = ref('')
const errorOperacion = ref('')

const ahoraMs = ref(Date.now())
let timer = null
let turnoComidaInicializado = false
const desuscripciones = []

const formComidaDia = reactive({
  turno: 'matutino',
  nombre: '',
  descripcion: '',
  precio: '30.00'
})

const formRegular = reactive({
  nombre: '',
  descripcion: '',
  precio: '',
  turno: 'ambos'
})

const filtrosHistorico = reactive({
  fecha: '',
  turno: '',
  modalidadPago: '',
  estado: ''
})

const historico = ref({
  pedidos: [],
  totalEntregados: 0,
  totalNoReclamados: 0,
  montoCobradoApp: 0,
  montoCobradoMostrador: 0,
  montoTotalCobrado: 0
})

const columnasCola = computed(() => [
  {
    estado: 'recibido',
    titulo: 'Recibidos',
    bordeSuperior: 'border-t-slate-400',
    items: cola.value.filter((p) => p.estadoPedido === 'recibido')
  },
  {
    estado: 'en_preparacion',
    titulo: 'En preparación',
    bordeSuperior: 'border-t-uv-azul',
    items: cola.value.filter((p) => p.estadoPedido === 'en_preparacion')
  },
  {
    estado: 'listo_para_recoger',
    titulo: 'Listos para recoger',
    bordeSuperior: 'border-t-uv-verde',
    items: cola.value.filter((p) => p.estadoPedido === 'listo_para_recoger')
  }
])

function siguienteEstado(actual) {
  if (actual === 'recibido') return 'en_preparacion'
  if (actual === 'en_preparacion') return 'listo_para_recoger'
  if (actual === 'listo_para_recoger') return 'entregado'
  return null
}

function textoAccionEstado(actual) {
  if (actual === 'recibido') return 'Iniciar preparación'
  if (actual === 'en_preparacion') return 'Marcar listo para recoger'
  if (actual === 'listo_para_recoger') return 'Validar y entregar'
  return ''
}

function formatearRestante(isoLimite) {
  if (!isoLimite) return '--:--'
  const diffSeg = Math.max(0, Math.floor((new Date(isoLimite).getTime() - ahoraMs.value) / 1000))
  const min = String(Math.floor(diffSeg / 60)).padStart(2, '0')
  const seg = String(diffSeg % 60).padStart(2, '0')
  return `${min}:${seg}`
}

async function alternarRecepcion() {
  errorOperacion.value = ''
  try {
    const { data } = await api.patch('/personal/operacion/recepcion', {
      abierta: !catalogo.value.recepcionAbierta
    })
    catalogo.value.recepcionAbierta = data.recepcionAbierta
    mensajeOperacion.value = data.recepcionAbierta
      ? 'Recepción de pedidos en línea abierta.'
      : 'Recepción de pedidos en línea cerrada temporalmente.'
  } catch (err) {
    errorOperacion.value = extraerMensajeError(err, 'No se pudo cambiar el estado de recepcion')
  }
}

async function cargarCola() {
  const { data } = await api.get('/personal/pedidos/cola')
  cola.value = data
}

async function cargarCatalogo() {
  const { data } = await api.get('/platillos')
  catalogo.value = data
  if (!turnoComidaInicializado && data?.turnoSugerido) {
    formComidaDia.turno = data.turnoSugerido
    turnoComidaInicializado = true
  }
}

async function cargarHistorico() {
  const params = {}
  if (filtrosHistorico.fecha) params.fecha = filtrosHistorico.fecha
  if (filtrosHistorico.turno) params.turno = filtrosHistorico.turno
  if (filtrosHistorico.modalidadPago) params.modalidadPago = filtrosHistorico.modalidadPago
  if (filtrosHistorico.estado) params.estado = filtrosHistorico.estado

  const { data } = await api.get('/personal/pedidos/historico', { params })
  historico.value = data
}

function abrirHistorico() {
  pestana.value = 'historico'
  cargarHistorico()
}

async function buscarReferencia() {
  errorOperacion.value = ''
  if (!referenciaBusqueda.value.trim()) return
  try {
    const { data } = await api.get('/personal/pedidos/buscar', {
      params: { referencia: referenciaBusqueda.value.trim() }
    })
    pedidoEncontrado.value = data
  } catch (err) {
    pedidoEncontrado.value = null
    errorOperacion.value = extraerMensajeError(err, 'No se encontró la referencia')
  }
}

function limpiarBusqueda() {
  referenciaBusqueda.value = ''
  pedidoEncontrado.value = null
}

function solicitarAvanceEstado(pedido) {
  if (pedido.estadoPedido === 'listo_para_recoger') {
    pedidoAEntregar.value = pedido
    return
  }
  avanzarEstado(pedido)
}

async function confirmarEntrega() {
  if (!pedidoAEntregar.value) return
  const pedido = pedidoAEntregar.value
  pedidoAEntregar.value = null
  await avanzarEstado(pedido)
}

async function avanzarEstado(pedido) {
  errorOperacion.value = ''
  mensajeOperacion.value = ''
  const siguiente = siguienteEstado(pedido.estadoPedido)
  if (!siguiente) return

  try {
    const { data } = await api.patch(`/personal/pedidos/${pedido.id}/estado`, {
      estadoPedido: siguiente
    })
    if (pedidoEncontrado.value?.id === data.id) {
      pedidoEncontrado.value = data
    }
    await cargarCola()
    mensajeOperacion.value = `Pedido ${data.referencia} actualizado a: ${etiquetaEstadoPedido(data.estadoPedido)}.`
  } catch (err) {
    errorOperacion.value = extraerMensajeError(err, 'No se pudo actualizar el pedido')
    await cargarCola()
  }
}

async function confirmarVencimiento() {
  if (!pedidoAVencer.value) return
  const pedido = pedidoAVencer.value
  pedidoAVencer.value = null
  errorOperacion.value = ''
  try {
    const { data } = await api.post(`/personal/pedidos/${pedido.id}/vencer`)
    if (pedidoEncontrado.value?.id === data.id) {
      pedidoEncontrado.value = data
    }
    await cargarCola()
    mensajeOperacion.value = `Pedido ${data.referencia} marcado como no reclamado. Se registró una falta al estudiante.`
  } catch (err) {
    errorOperacion.value = extraerMensajeError(err, 'No se pudo marcar como no reclamado')
    await cargarCola()
  }
}

async function alternarDisponibilidad(platillo) {
  errorOperacion.value = ''
  try {
    const nuevoEstado = !platillo.disponible
    await api.patch(`/personal/platillos/${platillo.id}/disponibilidad`, {
      disponible: nuevoEstado
    })
    await cargarCatalogo()
    mensajeOperacion.value = `${platillo.nombre} marcado como ${nuevoEstado ? 'disponible' : 'agotado'}.`
  } catch (err) {
    errorOperacion.value = extraerMensajeError(err, 'No se pudo cambiar la disponibilidad')
  }
}

function abrirEdicionPlatillo(platillo) {
  platilloEnEdicion.value = {
    id: platillo.id,
    nombre: platillo.nombre,
    descripcion: platillo.descripcion || '',
    precio: platillo.precio,
    turno: platillo.turno
  }
}

async function guardarEdicionPlatillo() {
  if (!platilloEnEdicion.value) return
  errorOperacion.value = ''
  try {
    await api.put(`/personal/platillos/regular/${platilloEnEdicion.value.id}`, {
      nombre: platilloEnEdicion.value.nombre,
      descripcion: platilloEnEdicion.value.descripcion,
      precio: Number(platilloEnEdicion.value.precio),
      turno: platilloEnEdicion.value.turno
    })
    platilloEnEdicion.value = null
    await cargarCatalogo()
    mensajeOperacion.value = 'Platillo actualizado en el menú.'
  } catch (err) {
    errorOperacion.value = extraerMensajeError(err, 'No se pudo actualizar el platillo')
  }
}

async function publicarComidaDelDia() {
  errorOperacion.value = ''
  try {
    await api.post('/personal/platillos/comida-del-dia', {
      nombre: formComidaDia.nombre,
      descripcion: formComidaDia.descripcion,
      precio: Number(formComidaDia.precio),
      turno: formComidaDia.turno
    })
    formComidaDia.nombre = ''
    formComidaDia.descripcion = ''
    await cargarCatalogo()
    mensajeOperacion.value = 'Comida del día publicada para el turno ' + formComidaDia.turno + '.'
  } catch (err) {
    errorOperacion.value = extraerMensajeError(err, 'No se pudo publicar la comida del dia')
  }
}

async function agregarPlatilloRegular() {
  errorOperacion.value = ''
  try {
    await api.post('/personal/platillos/regular', {
      nombre: formRegular.nombre,
      descripcion: formRegular.descripcion,
      precio: Number(formRegular.precio),
      turno: formRegular.turno
    })
    formRegular.nombre = ''
    formRegular.descripcion = ''
    formRegular.precio = ''
    await cargarCatalogo()
    mensajeOperacion.value = 'Platillo agregado al menú.'
  } catch (err) {
    errorOperacion.value = extraerMensajeError(err, 'No se pudo agregar el platillo')
  }
}

let respaldoTimer = null

onMounted(async () => {
  try {
    await Promise.all([cargarCola(), cargarCatalogo()])
  } catch (err) {
    errorOperacion.value = extraerMensajeError(err, 'No se pudo cargar la información inicial')
  }

  timer = setInterval(() => {
    ahoraMs.value = Date.now()
  }, 1000)

  respaldoTimer = setInterval(() => {
    if (!socketConectado.value) {
      const tareas = [cargarCola(), cargarCatalogo()]
      if (pestana.value === 'historico') {
        tareas.push(cargarHistorico())
      }
      Promise.all(tareas).catch(() => {})
    }
  }, 10000)

  desuscripciones.push(
    suscribirEvento('nuevo_pedido', () => {
      cargarCola()
    }),
    suscribirEvento('pedido_actualizado', (actualizado) => {
      cargarCola()
      if (pedidoEncontrado.value?.id === actualizado.id) {
        pedidoEncontrado.value = actualizado
      }
      if (pestana.value === 'historico') {
        cargarHistorico()
      }
    }),
    suscribirEvento('catalogo_actualizado', () => {
      cargarCatalogo()
    })
  )
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (respaldoTimer) clearInterval(respaldoTimer)
  desuscripciones.forEach((fn) => fn())
})
</script>
