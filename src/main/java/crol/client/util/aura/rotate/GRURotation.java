package crol.client.util.aura.rotate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class GRURotation {
   public static final String WEIGHTS_PATH = "config/myclient/gru_rotation.json";
   private static final int INPUT = 2;
   private static final int HIDDEN = 24;
   private static final int OUTPUT = 2;
   private double[][] Wz;
   private double[][] Uz;
   private double[] bz;
   private double[][] Wr;
   private double[][] Ur;
   private double[] br;
   private double[][] Wh;
   private double[][] Uh;
   private double[] bh;
   private double[][] Wy;
   private double[] by;
   private double meanYaw = (double)0.0F;
   private double meanPitch = (double)0.0F;
   private double stdYaw = (double)1.0F;
   private double stdPitch = (double)1.0F;
   private final double[] h = new double[24];
   private boolean loaded = false;
   private static GRURotation INSTANCE;

   public static GRURotation get() {
      if (INSTANCE == null) {
         INSTANCE = new GRURotation();
      }

      return INSTANCE;
   }

   private GRURotation() {
      this.initFallback();
      this.tryLoad();
   }

   public void reload() {
      this.tryLoad();
   }

   private void tryLoad() {
      if (Files.exists(Path.of("config/myclient/gru_rotation.json"), new LinkOption[0])) {
         this.load("config/myclient/gru_rotation.json");
      } else {
         System.out.println("[GRURotation] No weights file found, using fallback smoothing");
      }

   }

   public float[] forward(float dYaw, float dPitch) {
      double[] x = new double[]{((double)dYaw - this.meanYaw) / this.stdYaw, ((double)dPitch - this.meanPitch) / this.stdPitch};
      double[] z = this.sigmoid(this.addB(this.linear(this.Wz, x, this.Uz, this.h), this.bz));
      double[] r = this.sigmoid(this.addB(this.linear(this.Wr, x, this.Ur, this.h), this.br));
      double[] hc = this.tanh(this.addB(this.linear(this.Wh, x, this.Uh, this.hadamard(r, this.h)), this.bh));

      for(int i = 0; i < 24; ++i) {
         this.h[i] = ((double)1.0F - z[i]) * this.h[i] + z[i] * hc[i];
      }

      double[] y = this.addB(this.matVec(this.Wy, this.h), this.by);
      return new float[]{(float)(y[0] * this.stdYaw + this.meanYaw), (float)(y[1] * this.stdPitch + this.meanPitch)};
   }

   public void resetState() {
      Arrays.fill(this.h, (double)0.0F);
   }

   public boolean isLoaded() {
      return this.loaded;
   }

   public void load(String path) {
      try {
         String json = Files.readString(Path.of(path));
         JsonObject o = JsonParser.parseString(json).getAsJsonObject();
         this.Wz = this.mat(o, "Wz");
         this.Uz = this.mat(o, "Uz");
         this.bz = this.vec(o, "bz");
         this.Wr = this.mat(o, "Wr");
         this.Ur = this.mat(o, "Ur");
         this.br = this.vec(o, "br");
         this.Wh = this.mat(o, "Wh");
         this.Uh = this.mat(o, "Uh");
         this.bh = this.vec(o, "bh");
         this.Wy = this.mat(o, "Wy");
         this.by = this.vec(o, "by");
         if (o.has("meanYaw")) {
            this.meanYaw = o.get("meanYaw").getAsDouble();
         }

         if (o.has("meanPitch")) {
            this.meanPitch = o.get("meanPitch").getAsDouble();
         }

         if (o.has("stdYaw")) {
            this.stdYaw = o.get("stdYaw").getAsDouble();
         }

         if (o.has("stdPitch")) {
            this.stdPitch = o.get("stdPitch").getAsDouble();
         }

         this.loaded = true;
         System.out.println("[GRURotation] Weights loaded from: " + path);
      } catch (Exception e) {
         System.err.println("[GRURotation] Load failed: " + e.getMessage());
         this.initFallback();
      }

   }

   private void initFallback() {
      double s = 0.6;
      this.Wz = this.zeros2(24, 2);
      this.Uz = this.zeros2(24, 24);
      this.bz = this.fill(24, (double)-4.0F);
      this.Wr = this.zeros2(24, 2);
      this.Ur = this.zeros2(24, 24);
      this.br = this.fill(24, (double)4.0F);
      this.Wh = this.zeros2(24, 2);
      this.Uh = this.zeros2(24, 24);
      this.bh = this.zeros1(24);

      for(int i = 0; i < 24; ++i) {
         this.Wh[i][i % 2] = s;
      }

      this.Wy = this.zeros2(2, 24);
      this.by = this.zeros1(2);

      for(int i = 0; i < 2; ++i) {
         for(int j = 0; j < 24; ++j) {
            if (j % 2 == i) {
               this.Wy[i][j] = 0.08333333333333333;
            }
         }
      }

      this.loaded = false;
   }

   private double[] linear(double[][] W, double[] x, double[][] U, double[] hh) {
      double[] o = this.matVec(W, x);
      double[] u = this.matVec(U, hh);

      for(int i = 0; i < o.length; ++i) {
         o[i] += u[i];
      }

      return o;
   }

   private double[] matVec(double[][] M, double[] v) {
      double[] o = new double[M.length];

      for(int i = 0; i < M.length; ++i) {
         for(int j = 0; j < v.length; ++j) {
            o[i] += M[i][j] * v[j];
         }
      }

      return o;
   }

   private double[] addB(double[] a, double[] b) {
      double[] o = new double[a.length];

      for(int i = 0; i < a.length; ++i) {
         o[i] = a[i] + b[i];
      }

      return o;
   }

   private double[] hadamard(double[] a, double[] b) {
      double[] o = new double[a.length];

      for(int i = 0; i < a.length; ++i) {
         o[i] = a[i] * b[i];
      }

      return o;
   }

   private double[] sigmoid(double[] x) {
      double[] o = new double[x.length];

      for(int i = 0; i < x.length; ++i) {
         o[i] = (double)1.0F / ((double)1.0F + Math.exp(-x[i]));
      }

      return o;
   }

   private double[] tanh(double[] x) {
      double[] o = new double[x.length];

      for(int i = 0; i < x.length; ++i) {
         o[i] = Math.tanh(x[i]);
      }

      return o;
   }

   private double[][] zeros2(int r, int c) {
      return new double[r][c];
   }

   private double[] zeros1(int n) {
      return new double[n];
   }

   private double[] fill(int n, double v) {
      double[] a = new double[n];
      Arrays.fill(a, v);
      return a;
   }

   private double[][] mat(JsonObject o, String k) {
      JsonArray rows = o.getAsJsonArray(k);
      double[][] M = new double[rows.size()][];

      for(int i = 0; i < rows.size(); ++i) {
         JsonArray row = rows.get(i).getAsJsonArray();
         M[i] = new double[row.size()];

         for(int j = 0; j < row.size(); ++j) {
            M[i][j] = row.get(j).getAsDouble();
         }
      }

      return M;
   }

   private double[] vec(JsonObject o, String k) {
      JsonArray a = o.getAsJsonArray(k);
      double[] v = new double[a.size()];

      for(int i = 0; i < a.size(); ++i) {
         v[i] = a.get(i).getAsDouble();
      }

      return v;
   }
}
