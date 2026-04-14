package org.example.ducksocialnetworkm.domeniu.event;

import org.example.ducksocialnetworkm.domeniu.user.SwimmingDuck;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RaceEvent extends Event {

    private List<Double> distante;
    private List<SwimmingDuck> participanti;
    private String status;
    private String rezultatFinal;
    private double timpMinim;
    private Long[] culoarRata;
    private double[] culoarTimp;

    /**
     * Constructor
     * @param id - ID eveniment
     * @param nume - Nume eveniment
     * @param distante - Lista de distanțe pentru culoare
     */
    public RaceEvent(Long id, String nume, List<Double> distante, Long idCreator) {
        super(id, nume, idCreator);
        this.distante = distante;
        this.participanti = new ArrayList<>();
        this.status = "OPEN";
        this.rezultatFinal = "Evenimentul nu a început încă.";
    }


    public RaceEvent(Long id, String nume, List<Double> distante, List<SwimmingDuck> participanti, Long idCreator) {
        this(id, nume, distante, idCreator);
        if (participanti != null) {
            this.participanti = participanti;
        }
    }

    /**
     * Adaugă un participant la cursă (doar dacă e status OPEN)
     */
    public void adaugaParticipant(SwimmingDuck duck) {
        if (!"OPEN".equals(status)) {
            throw new IllegalStateException("Nu se mai pot face înscrieri!");
        }
        this.participanti.add(duck);
    }

    /**
     * Comparator pentru sortarea rațelor (necesar algoritmului)
     */
    static class SorteazaRezistenta implements Comparator<SwimmingDuck> {
        @Override
        public int compare(SwimmingDuck r1, SwimmingDuck r2) {
            if (r1.getRezistenta().equals(r2.getRezistenta()))
                return Double.compare(r2.getViteza(), r1.getViteza());
            return Double.compare(r2.getRezistenta(), r1.getRezistenta());
        }
    }

    @Override
    public void start() {
        if (!"OPEN".equals(status)) {
            System.out.println("Evenimentul nu este în starea OPEN.");
        }

        this.status = "IN_PROGRESS";
        notifySubscribers("Cursa " + nume + " a început!");

        if (participanti == null || participanti.isEmpty()) {
            this.rezultatFinal = "Eveniment anulat: Nu au fost participanți.";
            this.status = "FINISHED";
            notifySubscribers("Cursa anulată - lipsă participanți.");
            return;
        }

        List<SwimmingDuck> sortate = new ArrayList<>(participanti);
        Collections.sort(sortate, new SorteazaRezistenta());

        final double INF = 1e18;
        int n = sortate.size();
        int m = distante.size();

        double[][] pd = new double[n + 1][m + 1];
        boolean[][] folosita = new boolean[n + 1][m + 1];

        for (int i = 0; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                pd[i][j] = INF;
            }
        }

        for (int i = 0; i <= n; i++) pd[i][0] = 0.0;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= Math.min(i, m); j++) {
                pd[i][j] = pd[i - 1][j];

                double timpCurent = (2.0 * distante.get(j - 1)) / sortate.get(i - 1).getViteza();

                double timpCandidat = Math.max(pd[i - 1][j - 1], timpCurent);

                if (timpCandidat < pd[i][j]) {
                    pd[i][j] = timpCandidat;
                    folosita[i][j] = true;
                }
            }
        }

        // Reconstituire soluție
        this.timpMinim = pd[n][m];
        this.culoarRata = new Long[m];
        this.culoarTimp = new double[m];

        int i = n;
        int j = m;

        if (timpMinim >= INF) {
            this.rezultatFinal = "Nu au fost suficiente rațe pentru toate culoarele!";
            this.status = "FINISHED";
            return;
        }

        while (j > 0 && i > 0) {
            if (folosita[i][j]) {
                culoarRata[j - 1] = sortate.get(i - 1).getId();
                culoarTimp[j - 1] = (2.0 * distante.get(j - 1)) / sortate.get(i - 1).getViteza();
                j--;
                i--;
            } else {
                i--;
            }
        }

        this.status = "FINISHED";
        this.rezultatFinal = genereazaRaportString();
        notifySubscribers("Cursa " + nume + " s-a terminat! " + rezultatFinal);
    }

    /**
     * Generează raportul text, folosit atât în toString cât și pentru salvarea în DB.
     */
    private String genereazaRaportString() {
        if (!"FINISHED".equals(status)) return "Rezultate indisponibile.";

        DecimalFormat df = new DecimalFormat("0.000");
        df.setRoundingMode(RoundingMode.HALF_UP);
        StringBuilder sb = new StringBuilder();

        sb.append("Timp oficial cursă: ").append(df.format(timpMinim)).append(" secunde\n");
        sb.append("------------------------------------------------\n");

        for (int j = 0; j < distante.size(); j++) {
            sb.append("Culoar ").append(j + 1)
                    .append(" (").append(distante.get(j)).append("m): ");

            if (culoarRata[j] != null) {
                sb.append("Rata #").append(culoarRata[j])
                        .append(" -> ").append(df.format(culoarTimp[j])).append("s");
            } else {
                sb.append("Neocupat");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        if ("FINISHED".equals(status)) {
            return nume + " [FINALIZAT]\n" + rezultatFinal;
        } else if ("OPEN".equals(status)) {
            return nume + " [ÎNSCRIERI DESCHISE] - Distanțe: " + distante.toString() +
                    " | Participanți înscriși: " + participanti.size();
        } else {
            return nume + " [STATUS NECUNOSCUT]";
        }
    }

    public List<Double> getDistante() { return distante; }

    public List<SwimmingDuck> getParticipanti() { return participanti; }
    public void setParticipanti(List<SwimmingDuck> participanti) { this.participanti = participanti; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRezultatFinal() { return rezultatFinal; }
    public void setRezultatFinal(String rezultatFinal) { this.rezultatFinal = rezultatFinal; }
}