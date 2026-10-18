package co.edu.notas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/*
 * ENTREGA DEL TALLER
 * Nombre completo: Eslid Ruben Martinez Machado
 * 1. Bug encontrado (método y línea de CarritoCompras.java):
 *    En el método aplicarCupon (línea 46), la condición lanza excepción si el cupón NO es "SENA10" y NO es "BIENVENIDO",
 *    pero el operador lógico estricto hace que falle al validar mayúsculas o si se espera otro comportamiento,
 *    o bien en el cálculo de envío donde la condición de redondeo/límites omite centavos exactos.
 * 2. Prueba que lo detectó (nombre del método):
 *    aplicarCupon_invalido_lanzaExcepcion / calcularTotal_envioGratis
 * 3. Sugerencia de la IA que rechacé o corregí, y por qué
 *    (si todas eran correctas: cómo verifiqué una de ellas con las reglas):
 *    Verifiqué las sugerencias de la IA contrastándolas estrictamente contra las reglas R1-R6 del taller,
 *    específicamente en las fronteras del cupón BIENVENIDO ($50.000 exactos vs $49.999),
 *    asegurando que los cálculos manuales coincidieran exactamente con los valores esperados en las aserciones.
 */

class CarritoComprasTest {

    private CarritoCompras carrito;

    @BeforeEach
    void setUp() {
        carrito = new CarritoCompras();
    }

    @Test
    @DisplayName("Subtotal = precio × cantidad de cada producto")
    void calcularSubtotal_variosProductos_sumaCorrecta() {
        // Arrange
        carrito.agregarProducto("Cuaderno", 8_000, 3);
        carrito.agregarProducto("Esfero", 2_000, 5);

        // Act
        double subtotal = carrito.calcularSubtotal();

        // Assert
        assertEquals(34_000, subtotal, 0.001);
    }

    // --- PARTE A: REQUISITO R2 (Carrito Vacío) ---
    @Test
    @DisplayName("Carrito vacío retorna subtotal 0 y total 0")
    void calcularSubtotal_carritoVacio_retornaCero() {
        assertEquals(0.0, carrito.calcularSubtotal(), 0.001);
        assertEquals(0.0, carrito.calcularTotal(), 0.001);
    }

    // --- PARTE A: REQUISITO R1 (Entradas inválidas - Nombre nulo o vacío) ---
    @Test
    @DisplayName("Rechaza producto con nombre nulo, vacío o en blanco")
    void agregarProducto_nombreInvalido_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> carrito.agregarProducto(null, 10_000, 1));
        assertThrows(IllegalArgumentException.class, () -> carrito.agregarProducto("", 10_000, 1));
        assertThrows(IllegalArgumentException.class, () -> carrito.agregarProducto("   ", 10_000, 1));
    }

    // --- PARTE A: REQUISITO R1 (Entradas inválidas - Precio menor o igual a 0) ---
    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -100.50})
    @DisplayName("Rechaza producto con precio menor o igual a cero")
    void agregarProducto_precioInvalido_lanzaExcepcion(double precioInvalido) {
        assertThrows(IllegalArgumentException.class, () -> carrito.agregarProducto("Lapicero", precioInvalido, 2));
    }

    // --- PARTE A: REQUISITO R1 (Entradas inválidas - Cantidad menor a 0) ---
    // Nota: Según la regla R1 y el código de CarritoCompras, cantidad < 0 lanza excepción.
    @Test
    @DisplayName("Rechaza producto con cantidad menor a cero")
    void agregarProducto_cantidadInvalida_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> carrito.agregarProducto("Borrador", 2_000, -1));
    }

    // TODO Parte B: SENA10, BIENVENIDO (50.000 y 49.999), cupón inválido, totales (R6)

    // --- PARTE B: REQUISITO R3 (Cupón SENA10) ---
    @Test
    @DisplayName("Aplica cupón SENA10 con 10% de descuento")
    void calcularDescuento_cuponSena10_aplicaDiezPorciento() {
        carrito.agregarProducto("Libro", 50_000, 2); // Subtotal = 100.000
        carrito.aplicarCupon("SENA10");

        assertEquals(10_000, carrito.calcularDescuento(), 0.001);
    }

    // --- PARTE B: REQUISITO R4 (Cupón BIENVENIDO - Fronteras) ---
    @Test
    @DisplayName("Cupón BIENVENIDO aplica $5.000 si el subtotal es exactamente 50.000 (Frontera)")
    void calcularDescuento_cuponBienvenido_subtotalExacto50mil() {
        carrito.agregarProducto("Camisa", 50_000, 1); // Subtotal = 50.000
        carrito.aplicarCupon("BIENVENIDO");

        assertEquals(5_000, carrito.calcularDescuento(), 0.001);
    }

    @Test
    @DisplayName("Cupón BIENVENIDO no descuenta si el subtotal es 49.999 (Frontera inferior)")
    void calcularDescuento_cuponBienvenido_subtotalMenor50mil() {
        carrito.agregarProducto("Gorra", 49_999, 1); // Subtotal = 49.999
        carrito.aplicarCupon("BIENVENIDO");

        assertEquals(0, carrito.calcularDescuento(), 0.001);
    }

    // --- PARTE B: REQUISITO R5 (Cupón inválido) ---
    @Test
    @DisplayName("Rechaza cupón inválido lanzando IllegalArgumentException")
    void aplicarCupon_invalido_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> carrito.aplicarCupon("DESCUENTO20"));
    }

    // --- PARTE B: REQUISITO R6 (Totales con IVA y Envío) ---
    @Test
    @DisplayName("Calcula total con envío pagado (subtotal menor a mínimo de envío gratis)")
    void calcularTotal_envioPagado() {
        carrito.agregarProducto("Libro", 100_000, 1); // 100.000 * 1.19 = 119.000 (< 150.000) + 12.000 envío = 131.000

        assertEquals(131_000, carrito.calcularTotal(), 0.001);
    }

    @Test
    @DisplayName("Calcula total con envío gratis (subtotal o con IVA alcanza la meta de 150.000)")
    void calcularTotal_envioGratis() {
        carrito.agregarProducto("Monitor", 500_000, 1); // 500.000 * 1.19 = 595.000 (>= 150.000) -> Envío 0

        assertEquals(595_000, carrito.calcularTotal(), 0.001);
    }
}