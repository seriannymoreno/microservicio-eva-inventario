/* =============================================
   app.js — Lógica de la interfaz de Inventario
   API Base: http://localhost:8080/api/v1/inventario
   ============================================= */

// URL absoluta al servidor Spring Boot (puerto 8080)
const API_BASE = 'http://localhost:8080/api/v1/inventario';

// Estado global de paginación
let estado = {
  page: 0,
  size: 10,
  totalPages: 0,
  totalElements: 0,
  idParaEliminar: null,
};

/* ═══════════════════════════════════════════
   INICIALIZACIÓN
═══════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', () => {
  verificarConexion();
  cargarInventario();

  document.getElementById('searchId').addEventListener('keydown', e => {
    if (e.key === 'Enter') buscarPorId();
  });

  document.getElementById('modalOverlay').addEventListener('click', e => {
    if (e.target === e.currentTarget) cerrarModal();
  });
});

/* ═══════════════════════════════════════════
   VERIFICAR CONEXIÓN CON LA API
═══════════════════════════════════════════ */
async function verificarConexion() {
  const dot   = document.getElementById('statusDot');
  const label = document.getElementById('statusLabel');
  try {
    const res = await fetch(`${API_BASE}?page=0&size=1`);
    if (res.ok) {
      dot.className     = 'status-dot status-dot--ok';
      label.textContent = 'API conectada';
    } else {
      throw new Error('No OK');
    }
  } catch {
    dot.className     = 'status-dot status-dot--error';
    label.textContent = 'Sin conexión';
  }
}

/* ═══════════════════════════════════════════
   CARGAR INVENTARIO (tabla + paginación + KPI)
═══════════════════════════════════════════ */
async function cargarInventario() {
  const tbody = document.getElementById('inventarioBody');
  tbody.innerHTML = `
    <tr>
      <td colspan="7" class="table__empty">
        <div class="spinner"></div>
        <p>Cargando datos...</p>
      </td>
    </tr>`;

  try {
    const res = await fetch(`${API_BASE}?page=${estado.page}&size=${estado.size}`);

    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || `HTTP ${res.status}`);
    }

    const data = await res.json();

    // Actualizar estado global
    estado.totalPages    = data.totalPages    ?? 0;
    estado.totalElements = data.totalElements ?? 0;

    // Renderizar KPI
    document.getElementById('kpiTotal').textContent  = data.totalElements ?? '—';
    document.getElementById('kpiPage').textContent   = (data.page ?? 0) + 1;
    document.getElementById('kpiPages').textContent  = data.totalPages ?? '—';
    document.getElementById('kpiSize').textContent   = data.size ?? '—';

    // Renderizar tabla
    if (!data.content || data.content.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="7" class="table__empty">
            <p>No hay registros en esta página.</p>
          </td>
        </tr>`;
    } else {
      tbody.innerHTML = data.content.map(item => {
        const badge = stockBadge(item.cantidad, item.stock_minimo, item.stock_maximo);
        return `
          <tr>
            <td><span class="badge-id">#${item.id_inventario}</span></td>
            <td>${item.id_producto}</td>
            <td><strong>${formatNum(item.cantidad)}</strong></td>
            <td>${item.stock_minimo}</td>
            <td>${item.stock_maximo}</td>
            <td>${badge}</td>
            <td>
              <button class="btn--icon" title="Eliminar ID ${item.id_inventario}"
                      onclick="abrirModal(${item.id_inventario})">Eliminar</button>
            </td>
          </tr>`;
      }).join('');
    }

    // Renderizar paginación
    actualizarPaginacion(data);

  } catch (err) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" class="table__empty">
          <p class="text-error">Error al cargar datos: ${err.message}</p>
        </td>
      </tr>`;
    mostrarToast('error', `Error: ${err.message}`);
    verificarConexion();
  }
}

/* ═══════════════════════════════════════════
   BUSCAR POR ID
═══════════════════════════════════════════ */
async function buscarPorId() {
  const idRaw = document.getElementById('searchId').value.trim();
  const result = document.getElementById('searchResult');

  if (!idRaw || parseInt(idRaw) <= 0) {
    mostrarToast('error', 'Ingresa un ID válido (mayor a 0).');
    return;
  }

  const id = parseInt(idRaw);
  result.className = 'search-result hidden';

  try {
    const res = await fetch(`${API_BASE}/${id}`);

    if (!res.ok) {
      const err = await res.json();
      result.className = 'search-result search-result--error';
      result.innerHTML = `<span>Advertencia:</span> ${err.message || 'No se encontró el registro.'}`;
      result.classList.remove('hidden');
      return;
    }

    const item = await res.json();
    const badge = stockBadge(item.cantidad, item.stock_minimo, item.stock_maximo);

    result.className = 'search-result';
    result.innerHTML = `
      <div class="search-result__header">
        <span class="search-result__title">Inventario ID <strong>#${item.id_inventario}</strong></span>
        <div style="display:flex;gap:.6rem;align-items:center;">
          ${badge}
          <button class="btn btn--danger" style="padding:.35rem .85rem;font-size:.8rem"
                  onclick="abrirModal(${item.id_inventario})">Eliminar</button>
        </div>
      </div>
      <div class="search-result__grid">
        ${campo('ID Inventario', '#' + item.id_inventario)}
        ${campo('ID Producto',   item.id_producto)}
        ${campo('Cantidad',      formatNum(item.cantidad))}
        ${campo('Stock Minimo',  item.stock_minimo)}
        ${campo('Stock Maximo',  item.stock_maximo)}
      </div>`;
    result.classList.remove('hidden');

    mostrarToast('ok', `Inventario #${id} encontrado.`);

  } catch (err) {
    result.className = 'search-result search-result--error';
    result.innerHTML = `<span>Error de conexión:</span> ${err.message}`;
    result.classList.remove('hidden');
    mostrarToast('error', `Error: ${err.message}`);
  }
}

/* ═══════════════════════════════════════════
   ELIMINACIÓN — MODAL
═══════════════════════════════════════════ */
function abrirModal(id) {
  estado.idParaEliminar = id;
  document.getElementById('modalId').textContent = `#${id}`;
  document.getElementById('modalOverlay').classList.remove('hidden');
}

function cerrarModal() {
  estado.idParaEliminar = null;
  document.getElementById('modalOverlay').classList.add('hidden');
}

async function confirmarEliminacion() {
  const id = estado.idParaEliminar;
  cerrarModal();

  try {
    const res = await fetch(`${API_BASE}/${id}`, { method: 'DELETE' });

    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || `HTTP ${res.status}`);
    }

    mostrarToast('ok', `Inventario #${id} eliminado exitosamente.`);

    // Limpiar resultado de búsqueda si coincide
    const searchId = document.getElementById('searchId').value;
    if (parseInt(searchId) === id) {
      document.getElementById('searchResult').className = 'search-result hidden';
      document.getElementById('searchId').value = '';
    }

    // Ajustar página si queda vacía
    if (estado.page > 0 && estado.page >= estado.totalPages - 1) {
      estado.page--;
    }

    cargarInventario();

  } catch (err) {
    mostrarToast('error', `Error al eliminar: ${err.message}`);
  }
}

/* ═══════════════════════════════════════════
   PAGINACIÓN
═══════════════════════════════════════════ */
function actualizarPaginacion(data) {
  const pageInfo = document.getElementById('pageInfo');
  const btnFirst = document.getElementById('btnFirst');
  const btnPrev  = document.getElementById('btnPrev');
  const btnNext  = document.getElementById('btnNext');
  const btnLast  = document.getElementById('btnLast');

  const currentPage = data.page       ?? 0;
  const totalPages  = data.totalPages ?? 0;

  pageInfo.textContent = totalPages > 0
    ? `${currentPage + 1} / ${totalPages}`
    : '0 / 0';

  btnFirst.disabled = data.first ?? true;
  btnPrev.disabled  = data.first ?? true;
  btnNext.disabled  = data.last  ?? true;
  btnLast.disabled  = data.last  ?? true;
}

function irPrimera()      { estado.page = 0;                                        cargarInventario(); }
function paginaAnterior() { if (estado.page > 0) { estado.page--;                   cargarInventario(); } }
function paginaSiguiente(){ if (estado.page < estado.totalPages - 1) { estado.page++; cargarInventario(); } }
function irUltima()       { estado.page = Math.max(0, estado.totalPages - 1);       cargarInventario(); }

function cambiarSize() {
  estado.size = parseInt(document.getElementById('pageSize').value);
  estado.page = 0;
  cargarInventario();
}

/* ═══════════════════════════════════════════
   HELPERS DE UI
═══════════════════════════════════════════ */

/** Genera el badge de estado de stock (sin emojis) */
function stockBadge(cantidad, minimo, maximo) {
  if (cantidad <= 0)     return `<span class="stock-badge stock-badge--critical">Agotado</span>`;
  if (cantidad < minimo) return `<span class="stock-badge stock-badge--low">Stock bajo</span>`;
  if (cantidad > maximo) return `<span class="stock-badge stock-badge--overflow">Excedente</span>`;
  return                        `<span class="stock-badge stock-badge--ok">Normal</span>`;
}

/** Formatea números con decimales limpios */
function formatNum(n) {
  return Number.isInteger(n) ? n : parseFloat(n).toFixed(2);
}

/** Helper para campos del resultado de búsqueda */
function campo(nombre, valor) {
  return `
    <div class="search-result__field">
      <span class="search-result__field-name">${nombre}</span>
      <span class="search-result__field-value">${valor}</span>
    </div>`;
}

/* ── Toast de notificaciones ── */
let _toastTimer = null;

function mostrarToast(tipo, msg) {
  const toast     = document.getElementById('toast');
  const toastMsg  = document.getElementById('toastMsg');
  const toastIcon = document.getElementById('toastIcon');

  const labelMap = { ok: 'OK', error: 'Error', info: 'Info' };
  toastIcon.textContent = labelMap[tipo] ?? '';

  toastMsg.textContent = msg;
  toast.className = `toast toast--${tipo}`;
  toast.classList.remove('hidden');

  clearTimeout(_toastTimer);
  _toastTimer = setTimeout(() => toast.classList.add('hidden'), 3500);
}
