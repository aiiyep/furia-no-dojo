package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

/**
 * Fonte bitmap 5x7 desenhada pixel a pixel (estilo arcade).
 * Suporta A-Z, 0-9, pontuação básica e acentos do português
 * (agudo, til, circunflexo, crase e cedilha), desenhados por cima da letra.
 */
final class FontePixel {

    static final int LARGURA_GLIFO = 5;
    static final int ALTURA_GLIFO = 7;
    static final int AVANCO = 6;

    private static final Map<Character, boolean[][]> GLIFOS = new HashMap<>();

    // Acentos combinantes (forma decomposta NFD)
    private static final char AGUDO = '́';
    private static final char GRAVE = '̀';
    private static final char CIRCUNFLEXO = '̂';
    private static final char TIL = '̃';
    private static final char CEDILHA = '̧';

    private static final int[][] PIXELS_AGUDO = {{3, -2}, {2, -1}};
    private static final int[][] PIXELS_GRAVE = {{1, -2}, {2, -1}};
    private static final int[][] PIXELS_CIRCUNFLEXO = {{1, -1}, {2, -2}, {3, -1}};
    private static final int[][] PIXELS_TIL = {{0, -1}, {1, -2}, {2, -1}, {3, -2}};
    private static final int[][] PIXELS_CEDILHA = {{2, 7}, {1, 8}};

    static {
        definir('A', ".###.", "#...#", "#...#", "#####", "#...#", "#...#", "#...#");
        definir('B', "####.", "#...#", "#...#", "####.", "#...#", "#...#", "####.");
        definir('C', ".###.", "#...#", "#....", "#....", "#....", "#...#", ".###.");
        definir('D', "####.", "#...#", "#...#", "#...#", "#...#", "#...#", "####.");
        definir('E', "#####", "#....", "#....", "####.", "#....", "#....", "#####");
        definir('F', "#####", "#....", "#....", "####.", "#....", "#....", "#....");
        definir('G', ".###.", "#...#", "#....", "#.###", "#...#", "#...#", ".####");
        definir('H', "#...#", "#...#", "#...#", "#####", "#...#", "#...#", "#...#");
        definir('I', ".###.", "..#..", "..#..", "..#..", "..#..", "..#..", ".###.");
        definir('J', "..###", "...#.", "...#.", "...#.", "#..#.", "#..#.", ".##..");
        definir('K', "#...#", "#..#.", "#.#..", "##...", "#.#..", "#..#.", "#...#");
        definir('L', "#....", "#....", "#....", "#....", "#....", "#....", "#####");
        definir('M', "#...#", "##.##", "#.#.#", "#.#.#", "#...#", "#...#", "#...#");
        definir('N', "#...#", "#...#", "##..#", "#.#.#", "#..##", "#...#", "#...#");
        definir('O', ".###.", "#...#", "#...#", "#...#", "#...#", "#...#", ".###.");
        definir('P', "####.", "#...#", "#...#", "####.", "#....", "#....", "#....");
        definir('Q', ".###.", "#...#", "#...#", "#...#", "#.#.#", "#..#.", ".##.#");
        definir('R', "####.", "#...#", "#...#", "####.", "#.#..", "#..#.", "#...#");
        definir('S', ".####", "#....", "#....", ".###.", "....#", "....#", "####.");
        definir('T', "#####", "..#..", "..#..", "..#..", "..#..", "..#..", "..#..");
        definir('U', "#...#", "#...#", "#...#", "#...#", "#...#", "#...#", ".###.");
        definir('V', "#...#", "#...#", "#...#", "#...#", "#...#", ".#.#.", "..#..");
        definir('W', "#...#", "#...#", "#...#", "#.#.#", "#.#.#", "#.#.#", ".#.#.");
        definir('X', "#...#", "#...#", ".#.#.", "..#..", ".#.#.", "#...#", "#...#");
        definir('Y', "#...#", "#...#", ".#.#.", "..#..", "..#..", "..#..", "..#..");
        definir('Z', "#####", "....#", "...#.", "..#..", ".#...", "#....", "#####");

        definir('0', ".###.", "#...#", "#..##", "#.#.#", "##..#", "#...#", ".###.");
        definir('1', "..#..", ".##..", "..#..", "..#..", "..#..", "..#..", ".###.");
        definir('2', ".###.", "#...#", "....#", "...#.", "..#..", ".#...", "#####");
        definir('3', "####.", "....#", "....#", ".###.", "....#", "....#", "####.");
        definir('4', "...#.", "..##.", ".#.#.", "#..#.", "#####", "...#.", "...#.");
        definir('5', "#####", "#....", "####.", "....#", "....#", "#...#", ".###.");
        definir('6', ".###.", "#....", "#....", "####.", "#...#", "#...#", ".###.");
        definir('7', "#####", "....#", "...#.", "..#..", ".#...", ".#...", ".#...");
        definir('8', ".###.", "#...#", "#...#", ".###.", "#...#", "#...#", ".###.");
        definir('9', ".###.", "#...#", "#...#", ".####", "....#", "....#", ".###.");

        definir('.', ".....", ".....", ".....", ".....", ".....", ".##..", ".##..");
        definir(',', ".....", ".....", ".....", ".....", ".....", "..#..", ".#...");
        definir('!', "..#..", "..#..", "..#..", "..#..", "..#..", ".....", "..#..");
        definir('?', ".###.", "#...#", "....#", "...#.", "..#..", ".....", "..#..");
        definir(':', ".....", ".##..", ".##..", ".....", ".##..", ".##..", ".....");
        definir('-', ".....", ".....", ".....", "#####", ".....", ".....", ".....");
        definir('+', ".....", "..#..", "..#..", "#####", "..#..", "..#..", ".....");
        definir('=', ".....", ".....", "#####", ".....", "#####", ".....", ".....");
        definir('/', "....#", "....#", "...#.", "..#..", ".#...", "#....", "#....");
        definir('(', "...#.", "..#..", ".#...", ".#...", ".#...", "..#..", "...#.");
        definir(')', ".#...", "..#..", "...#.", "...#.", "...#.", "..#..", ".#...");
        definir('<', "...#.", "..#..", ".#...", "#....", ".#...", "..#..", "...#.");
        definir('>', ".#...", "..#..", "...#.", "....#", "...#.", "..#..", ".#...");
        definir('\'', "..#..", "..#..", ".....", ".....", ".....", ".....", ".....");
        definir('*', ".....", "#.#.#", ".###.", "#####", ".###.", "#.#.#", ".....");
        definir(' ', ".....", ".....", ".....", ".....", ".....", ".....", ".....");
    }

    private FontePixel() {
    }

    private static void definir(char c, String... linhas) {
        boolean[][] glifo = new boolean[ALTURA_GLIFO][LARGURA_GLIFO];
        for (int y = 0; y < ALTURA_GLIFO; y++) {
            for (int x = 0; x < LARGURA_GLIFO; x++) {
                glifo[y][x] = linhas[y].charAt(x) == '#';
            }
        }
        GLIFOS.put(c, glifo);
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.toUpperCase(), Normalizer.Form.NFD);
    }

    private static boolean isMarca(char c) {
        return Character.getType(c) == Character.NON_SPACING_MARK;
    }

    private static int contarCaracteres(String nfd) {
        int n = 0;
        for (int i = 0; i < nfd.length(); i++) {
            if (!isMarca(nfd.charAt(i))) {
                n++;
            }
        }
        return n;
    }

    /** Largura do texto em pixels (na resolução baixa). */
    static int largura(String texto, int escala) {
        int n = contarCaracteres(normalizar(texto));
        return n == 0 ? 0 : (n * AVANCO - 1) * escala;
    }

    /** Altura do texto em pixels (na resolução baixa). */
    static int altura(int escala) {
        return ALTURA_GLIFO * escala;
    }

    /** Maior escala (até {@code escalaMaxima}) em que o texto cabe na largura dada. */
    static int escalaQueCabe(String texto, int larguraMaxima, int escalaMaxima) {
        for (int e = escalaMaxima; e > 1; e--) {
            if (largura(texto, e) <= larguraMaxima) {
                return e;
            }
        }
        return 1;
    }

    static void desenhar(Graphics2D g, String texto, int x, int y, int escala, Color cor, Color contorno) {
        desenhar(g, texto, x, y, escala, cor, cor, contorno);
    }

    static void desenharCentralizado(Graphics2D g, String texto, int centroX, int y, int escala,
                                     Color topo, Color base, Color contorno) {
        desenhar(g, texto, centroX - largura(texto, escala) / 2, y, escala, topo, base, contorno);
    }

    static void desenharDireita(Graphics2D g, String texto, int direitaX, int y, int escala,
                                Color cor, Color contorno) {
        desenhar(g, texto, direitaX - largura(texto, escala), y, escala, cor, cor, contorno);
    }

    /**
     * Desenha o texto com degradê vertical em faixas (cor do topo até a cor
     * da base) e contorno de 1 pixel.
     */
    static void desenhar(Graphics2D g, String texto, int x, int y, int escala,
                         Color topo, Color base, Color contorno) {
        String nfd = normalizar(texto);
        for (int passo = 0; passo < 2; passo++) {
            boolean passoContorno = passo == 0;
            if (passoContorno && contorno == null) {
                continue;
            }
            int cx = x;
            for (int i = 0; i < nfd.length(); i++) {
                char c = nfd.charAt(i);
                if (isMarca(c)) {
                    continue;
                }
                char marca = (i + 1 < nfd.length() && isMarca(nfd.charAt(i + 1))) ? nfd.charAt(i + 1) : 0;
                desenharGlifo(g, c, marca, cx, y, escala, passoContorno, topo, base, contorno);
                cx += AVANCO * escala;
            }
        }
    }

    private static void desenharGlifo(Graphics2D g, char c, char marca, int x, int y, int escala,
                                      boolean passoContorno, Color topo, Color base, Color contorno) {
        boolean[][] glifo = GLIFOS.get(c);
        if (glifo == null) {
            glifo = GLIFOS.get('?');
        }
        for (int linha = 0; linha < ALTURA_GLIFO; linha++) {
            for (int coluna = 0; coluna < LARGURA_GLIFO; coluna++) {
                if (glifo[linha][coluna]) {
                    pixel(g, x, y, coluna, linha, escala, passoContorno, topo, base, contorno);
                }
            }
        }
        int[][] pixelsMarca = pixelsDaMarca(marca);
        if (pixelsMarca != null) {
            for (int[] p : pixelsMarca) {
                pixel(g, x, y, p[0], p[1], escala, passoContorno, topo, base, contorno);
            }
        }
    }

    private static int[][] pixelsDaMarca(char marca) {
        switch (marca) {
            case AGUDO:
                return PIXELS_AGUDO;
            case GRAVE:
                return PIXELS_GRAVE;
            case CIRCUNFLEXO:
                return PIXELS_CIRCUNFLEXO;
            case TIL:
                return PIXELS_TIL;
            case CEDILHA:
                return PIXELS_CEDILHA;
            default:
                return null;
        }
    }

    private static void pixel(Graphics2D g, int x, int y, int coluna, int linha, int escala,
                              boolean passoContorno, Color topo, Color base, Color contorno) {
        int px = x + coluna * escala;
        int py = y + linha * escala;
        if (passoContorno) {
            int borda = escala >= 4 ? 2 : 1; // letreiros grandes ganham contorno mais grosso
            g.setColor(contorno);
            g.fillRect(px - borda, py - borda, escala + 2 * borda, escala + 2 * borda);
        } else {
            double t = Math.max(0, Math.min(ALTURA_GLIFO - 1, linha)) / (double) (ALTURA_GLIFO - 1);
            g.setColor(Paleta.misturar(topo, base, t));
            g.fillRect(px, py, escala, escala);
        }
    }
}
