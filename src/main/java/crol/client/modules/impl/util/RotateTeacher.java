package crol.client.modules.impl.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.IEnableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RotateTeacher extends Module implements IDisableable, IEnableable, ITickable, IUtil {
   private static final int HIDDEN = 24;
   private static final int INPUT = 2;
   private static final int OUTPUT = 2;
   private static final int SEQ_LEN = 16;
   private static final int EPOCHS = 300;
   private static final double LR = 0.001;
   private static final double CLIP_NORM = (double)1.0F;
   private static final int BATCH_SIZE = 32;
   private static final String DATASET_PATH = "config/myclient/gru_dataset.json";
   private static final String WEIGHTS_PATH = "config/myclient/gru_rotation.json";
   private final List<float[]> samples = new ArrayList();
   private float prevYaw = 0.0F;
   private float prevPitch = 0.0F;
   private boolean firstTick = true;
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
   private double[][] dWz;
   private double[][] dUz;
   private double[][] dWr;
   private double[][] dUr;
   private double[][] dWh;
   private double[][] dUh;
   private double[][] dWy;
   private double[] dbz;
   private double[] dbr;
   private double[] dbh;
   private double[] dby;
   private double[][] mWz;
   private double[][] vWz;
   private double[][] mUz;
   private double[][] vUz;
   private double[][] mWr;
   private double[][] vWr;
   private double[][] mUr;
   private double[][] vUr;
   private double[][] mWh;
   private double[][] vWh;
   private double[][] mUh;
   private double[][] vUh;
   private double[][] mWy;
   private double[][] vWy;
   private double[] mbz;
   private double[] vbz;
   private double[] mbr;
   private double[] vbr;
   private double[] mbh;
   private double[] vbh;
   private double[] mby;
   private double[] vby;
   private int adamStep = 0;
   private double meanYaw;
   private double meanPitch;
   private double stdYaw;
   private double stdPitch;

   public RotateTeacher() {
      super(new ModuleInfo("RotateTeacher", Category.UTIL, "Records mouse & trains GRU rotation"));
      this.initWeights();
   }

   public void onEnable() {
      this.samples.clear();
      this.firstTick = true;
      System.out.println("[RotateTeacher] Recording started...");
   }

   public void onDisable() {
      System.out.println("[RotateTeacher] Recording stopped. Samples: " + this.samples.size());
      if (this.samples.size() < 17) {
         System.out.println("[RotateTeacher] Not enough data (need >17)");
      } else {
         this.saveDataset();
         System.out.println("[RotateTeacher] Training...");
         this.train();
         this.saveWeights();
         System.out.println("[RotateTeacher] Done! Weights saved to config/myclient/gru_rotation.json");
      }
   }

   public void onTick(TickEvent event) {
      if (mc.player != null) {
         float yaw = mc.player.getYaw();
         float pitch = mc.player.getPitch();
         if (this.firstTick) {
            this.prevYaw = yaw;
            this.prevPitch = pitch;
            this.firstTick = false;
         } else {
            float dYaw = this.wrapDeg(yaw - this.prevYaw);
            float dPitch = this.wrapDeg(pitch - this.prevPitch);
            this.samples.add(new float[]{dYaw, dPitch});
            this.prevYaw = yaw;
            this.prevPitch = pitch;
         }
      }
   }

   private void train() {
      this.computeNorm();
      double[][] data = this.normalize();
      int N = data.length - 16;
      double[][][] X = new double[N][16][2];
      double[][][] Y = new double[N][16][2];

      for(int i = 0; i < N; ++i) {
         for(int t = 0; t < 16; ++t) {
            X[i][t] = data[i + t];
            Y[i][t] = data[i + t + 1];
         }
      }

      for(int epoch = 1; epoch <= 300; ++epoch) {
         Integer[] idx = new Integer[N];

         for(int i = 0; i < N; ++i) {
            idx[i] = i;
         }

         this.shuffleArray(idx);
         double epochLoss = (double)0.0F;
         int batches = 0;

         for(int b = 0; b < N; b += 32) {
            int end = Math.min(b + 32, N);
            this.zeroGrads();
            double batchLoss = (double)0.0F;

            for(int i = b; i < end; ++i) {
               batchLoss += this.forwardBackward(X[idx[i]], Y[idx[i]]);
            }

            batchLoss /= (double)(end - b);
            epochLoss += batchLoss;
            ++batches;
            this.clipGrads();
            ++this.adamStep;
            this.adamUpdate();
         }

         if (epoch % 30 == 0) {
            System.out.printf("[RotateTeacher] Epoch %d/%d  loss=%.6f%n", epoch, 300, epochLoss / (double)batches);
         }
      }

   }

   private double forwardBackward(double[][] x, double[][] y) {
      double[][] hs = new double[17][24];
      double[][] zs = new double[16][24];
      double[][] rs = new double[16][24];
      double[][] hcs = new double[16][24];
      double[][] out = new double[16][2];
      hs[0] = new double[24];

      for(int t = 0; t < 16; ++t) {
         double[] ht = hs[t];
         zs[t] = this.sigmoid(this.addB(this.linear(this.Wz, x[t], this.Uz, ht), this.bz));
         rs[t] = this.sigmoid(this.addB(this.linear(this.Wr, x[t], this.Ur, ht), this.br));
         hcs[t] = this.tanh(this.addB(this.linear(this.Wh, x[t], this.Uh, this.hadamard(rs[t], ht)), this.bh));
         double[] hn = new double[24];

         for(int i = 0; i < 24; ++i) {
            hn[i] = ((double)1.0F - zs[t][i]) * ht[i] + zs[t][i] * hcs[t][i];
         }

         hs[t + 1] = hn;
         out[t] = this.addB(this.matVec(this.Wy, hn), this.by);
      }

      double loss = (double)0.0F;
      double[][] dOut = new double[16][2];

      for(int t = 0; t < 16; ++t) {
         for(int i = 0; i < 2; ++i) {
            double err = out[t][i] - y[t][i];
            dOut[t][i] = (double)2.0F * err / (double)32.0F;
            loss += err * err;
         }
      }

      loss /= (double)32.0F;
      double[] dh_next = new double[24];

      for(int t = 15; t >= 0; --t) {
         for(int i = 0; i < 2; ++i) {
            double[] var10000 = this.dby;
            var10000[i] += dOut[t][i];

            for(int j = 0; j < 24; ++j) {
               var10000 = this.dWy[i];
               var10000[j] += dOut[t][i] * hs[t + 1][j];
            }
         }

         double[] dh = this.matVecT(this.Wy, dOut[t]);

         for(int i = 0; i < 24; ++i) {
            dh[i] += dh_next[i];
         }

         double[] dhc = new double[24];
         double[] dz = new double[24];
         double[] dh_prev = new double[24];

         for(int i = 0; i < 24; ++i) {
            dhc[i] = dh[i] * zs[t][i];
            dz[i] = dh[i] * (hcs[t][i] - hs[t][i]);
            dh_prev[i] = dh[i] * ((double)1.0F - zs[t][i]);
         }

         double[] dhc_raw = new double[24];

         for(int i = 0; i < 24; ++i) {
            dhc_raw[i] = dhc[i] * ((double)1.0F - hcs[t][i] * hcs[t][i]);
         }

         for(int i = 0; i < 24; ++i) {
            double[] var45 = this.dbh;
            var45[i] += dhc_raw[i];
         }

         double[] rh = this.hadamard(rs[t], hs[t]);
         this.addOuterGrad(this.dWh, dhc_raw, x[t]);
         this.addOuterGrad(this.dUh, dhc_raw, rh);
         double[] dr = this.matVecT(this.Uh, dhc_raw);
         double[] dh_from_Uh = new double[24];
         double[] dr_raw = new double[24];

         for(int i = 0; i < 24; ++i) {
            dh_from_Uh[i] = dr[i] * rs[t][i];
            dr_raw[i] = dr[i] * hs[t][i] * rs[t][i] * ((double)1.0F - rs[t][i]);
         }

         for(int i = 0; i < 24; ++i) {
            double[] var46 = this.dbr;
            var46[i] += dr_raw[i];
         }

         this.addOuterGrad(this.dWr, dr_raw, x[t]);
         this.addOuterGrad(this.dUr, dr_raw, hs[t]);
         double[] dz_raw = new double[24];

         for(int i = 0; i < 24; ++i) {
            dz_raw[i] = dz[i] * zs[t][i] * ((double)1.0F - zs[t][i]);
         }

         for(int i = 0; i < 24; ++i) {
            double[] var47 = this.dbz;
            var47[i] += dz_raw[i];
         }

         this.addOuterGrad(this.dWz, dz_raw, x[t]);
         this.addOuterGrad(this.dUz, dz_raw, hs[t]);
         double[] dh_from_Uz = this.matVecT(this.Uz, dz_raw);
         double[] dh_from_Ur = this.matVecT(this.Ur, dr_raw);
         this.matVecT(this.Uh, dhc_raw);

         for(int i = 0; i < 24; ++i) {
            dh_next[i] = dh_prev[i] + dh_from_Uz[i] + dh_from_Ur[i] + dh_from_Uh[i];
         }
      }

      return loss;
   }

   private void adamUpdate() {
      double b1 = 0.9;
      double b2 = 0.999;
      double eps = 1.0E-8;
      double bc1 = (double)1.0F - Math.pow(b1, (double)this.adamStep);
      double bc2 = (double)1.0F - Math.pow(b2, (double)this.adamStep);
      this.adamMat(this.Wz, this.dWz, this.mWz, this.vWz, bc1, bc2, b1, b2, eps, 0.001);
      this.adamMat(this.Uz, this.dUz, this.mUz, this.vUz, bc1, bc2, b1, b2, eps, 0.001);
      this.adamVec(this.bz, this.dbz, this.mbz, this.vbz, bc1, bc2, b1, b2, eps, 0.001);
      this.adamMat(this.Wr, this.dWr, this.mWr, this.vWr, bc1, bc2, b1, b2, eps, 0.001);
      this.adamMat(this.Ur, this.dUr, this.mUr, this.vUr, bc1, bc2, b1, b2, eps, 0.001);
      this.adamVec(this.br, this.dbr, this.mbr, this.vbr, bc1, bc2, b1, b2, eps, 0.001);
      this.adamMat(this.Wh, this.dWh, this.mWh, this.vWh, bc1, bc2, b1, b2, eps, 0.001);
      this.adamMat(this.Uh, this.dUh, this.mUh, this.vUh, bc1, bc2, b1, b2, eps, 0.001);
      this.adamVec(this.bh, this.dbh, this.mbh, this.vbh, bc1, bc2, b1, b2, eps, 0.001);
      this.adamMat(this.Wy, this.dWy, this.mWy, this.vWy, bc1, bc2, b1, b2, eps, 0.001);
      this.adamVec(this.by, this.dby, this.mby, this.vby, bc1, bc2, b1, b2, eps, 0.001);
   }

   private void adamMat(double[][] W, double[][] dW, double[][] m, double[][] v, double bc1, double bc2, double b1, double b2, double eps, double lr) {
      for(int i = 0; i < W.length; ++i) {
         for(int j = 0; j < W[0].length; ++j) {
            m[i][j] = b1 * m[i][j] + ((double)1.0F - b1) * dW[i][j];
            v[i][j] = b2 * v[i][j] + ((double)1.0F - b2) * dW[i][j] * dW[i][j];
            W[i][j] -= lr * (m[i][j] / bc1) / (Math.sqrt(v[i][j] / bc2) + eps);
         }
      }

   }

   private void adamVec(double[] w, double[] dw, double[] m, double[] v, double bc1, double bc2, double b1, double b2, double eps, double lr) {
      for(int i = 0; i < w.length; ++i) {
         m[i] = b1 * m[i] + ((double)1.0F - b1) * dw[i];
         v[i] = b2 * v[i] + ((double)1.0F - b2) * dw[i] * dw[i];
         w[i] -= lr * (m[i] / bc1) / (Math.sqrt(v[i] / bc2) + eps);
      }

   }

   private void clipGrads() {
      double norm = (double)0.0F;
      norm += this.normMat(this.dWz);
      norm += this.normMat(this.dUz);
      norm += this.normVec(this.dbz);
      norm += this.normMat(this.dWr);
      norm += this.normMat(this.dUr);
      norm += this.normVec(this.dbr);
      norm += this.normMat(this.dWh);
      norm += this.normMat(this.dUh);
      norm += this.normVec(this.dbh);
      norm += this.normMat(this.dWy);
      norm += this.normVec(this.dby);
      norm = Math.sqrt(norm);
      if (norm > (double)1.0F) {
         double scale = (double)1.0F / norm;
         this.scaleMat(this.dWz, scale);
         this.scaleMat(this.dUz, scale);
         this.scaleVec(this.dbz, scale);
         this.scaleMat(this.dWr, scale);
         this.scaleMat(this.dUr, scale);
         this.scaleVec(this.dbr, scale);
         this.scaleMat(this.dWh, scale);
         this.scaleMat(this.dUh, scale);
         this.scaleVec(this.dbh, scale);
         this.scaleMat(this.dWy, scale);
         this.scaleVec(this.dby, scale);
      }

   }

   private void initWeights() {
      Random rng = new Random(42L);
      this.Wz = this.xavier(24, 2, rng);
      this.Uz = this.xavier(24, 24, rng);
      this.bz = this.fill(24, (double)-2.0F);
      this.Wr = this.xavier(24, 2, rng);
      this.Ur = this.xavier(24, 24, rng);
      this.br = this.fill(24, (double)2.0F);
      this.Wh = this.xavier(24, 2, rng);
      this.Uh = this.xavier(24, 24, rng);
      this.bh = this.zeros1(24);
      this.Wy = this.xavier(2, 24, rng);
      this.by = this.zeros1(2);
      this.dWz = this.zeros2(24, 2);
      this.dUz = this.zeros2(24, 24);
      this.dbz = this.zeros1(24);
      this.dWr = this.zeros2(24, 2);
      this.dUr = this.zeros2(24, 24);
      this.dbr = this.zeros1(24);
      this.dWh = this.zeros2(24, 2);
      this.dUh = this.zeros2(24, 24);
      this.dbh = this.zeros1(24);
      this.dWy = this.zeros2(2, 24);
      this.dby = this.zeros1(2);
      this.mWz = this.zeros2(24, 2);
      this.vWz = this.zeros2(24, 2);
      this.mUz = this.zeros2(24, 24);
      this.vUz = this.zeros2(24, 24);
      this.mbz = this.zeros1(24);
      this.vbz = this.zeros1(24);
      this.mWr = this.zeros2(24, 2);
      this.vWr = this.zeros2(24, 2);
      this.mUr = this.zeros2(24, 24);
      this.vUr = this.zeros2(24, 24);
      this.mbr = this.zeros1(24);
      this.vbr = this.zeros1(24);
      this.mWh = this.zeros2(24, 2);
      this.vWh = this.zeros2(24, 2);
      this.mUh = this.zeros2(24, 24);
      this.vUh = this.zeros2(24, 24);
      this.mbh = this.zeros1(24);
      this.vbh = this.zeros1(24);
      this.mWy = this.zeros2(2, 24);
      this.vWy = this.zeros2(2, 24);
      this.mby = this.zeros1(2);
      this.vby = this.zeros1(2);
   }

   private void zeroGrads() {
      this.zeroMat(this.dWz);
      this.zeroMat(this.dUz);
      this.zeroVec(this.dbz);
      this.zeroMat(this.dWr);
      this.zeroMat(this.dUr);
      this.zeroVec(this.dbr);
      this.zeroMat(this.dWh);
      this.zeroMat(this.dUh);
      this.zeroVec(this.dbh);
      this.zeroMat(this.dWy);
      this.zeroVec(this.dby);
   }

   private void computeNorm() {
      double sy = (double)0.0F;
      double sp = (double)0.0F;
      double sy2 = (double)0.0F;
      double sp2 = (double)0.0F;
      int n = this.samples.size();

      for(float[] s : this.samples) {
         sy += (double)s[0];
         sp += (double)s[1];
      }

      this.meanYaw = sy / (double)n;
      this.meanPitch = sp / (double)n;

      for(float[] s : this.samples) {
         sy2 += ((double)s[0] - this.meanYaw) * ((double)s[0] - this.meanYaw);
         sp2 += ((double)s[1] - this.meanPitch) * ((double)s[1] - this.meanPitch);
      }

      this.stdYaw = Math.sqrt(sy2 / (double)n) + 1.0E-8;
      this.stdPitch = Math.sqrt(sp2 / (double)n) + 1.0E-8;
   }

   private double[][] normalize() {
      double[][] d = new double[this.samples.size()][2];

      for(int i = 0; i < this.samples.size(); ++i) {
         d[i][0] = ((double)((float[])this.samples.get(i))[0] - this.meanYaw) / this.stdYaw;
         d[i][1] = ((double)((float[])this.samples.get(i))[1] - this.meanPitch) / this.stdPitch;
      }

      return d;
   }

   private void saveWeights() {
      try {
         Files.createDirectories(Path.of("config/myclient"));
         JsonObject o = new JsonObject();
         o.add("Wz", this.matToJson(this.Wz));
         o.add("Uz", this.matToJson(this.Uz));
         o.add("bz", this.vecToJson(this.bz));
         o.add("Wr", this.matToJson(this.Wr));
         o.add("Ur", this.matToJson(this.Ur));
         o.add("br", this.vecToJson(this.br));
         o.add("Wh", this.matToJson(this.Wh));
         o.add("Uh", this.matToJson(this.Uh));
         o.add("bh", this.vecToJson(this.bh));
         o.add("Wy", this.matToJson(this.Wy));
         o.add("by", this.vecToJson(this.by));
         o.addProperty("meanYaw", this.meanYaw);
         o.addProperty("meanPitch", this.meanPitch);
         o.addProperty("stdYaw", this.stdYaw);
         o.addProperty("stdPitch", this.stdPitch);
         Files.writeString(Path.of("config/myclient/gru_rotation.json"), (new GsonBuilder()).setPrettyPrinting().create().toJson(o));
      } catch (Exception e) {
         System.err.println("[RotateTeacher] save weights failed: " + e.getMessage());
      }

   }

   private void saveDataset() {
      try {
         Files.createDirectories(Path.of("config/myclient"));
         JsonArray root = new JsonArray();

         for(float[] s : this.samples) {
            JsonArray pair = new JsonArray();
            pair.add(s[0]);
            pair.add(s[1]);
            root.add(pair);
         }

         Files.writeString(Path.of("config/myclient/gru_dataset.json"), (new Gson()).toJson(root));
      } catch (Exception e) {
         System.err.println("[RotateTeacher] save dataset failed: " + e.getMessage());
      }

   }

   private double[] linear(double[][] W, double[] x, double[][] U, double[] h) {
      double[] o = this.matVec(W, x);
      double[] u = this.matVec(U, h);

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

   private double[] matVecT(double[][] M, double[] v) {
      double[] o = new double[M[0].length];

      for(int i = 0; i < M.length; ++i) {
         for(int j = 0; j < M[0].length; ++j) {
            o[j] += M[i][j] * v[i];
         }
      }

      return o;
   }

   private void addOuterGrad(double[][] dW, double[] a, double[] b) {
      for(int i = 0; i < a.length; ++i) {
         for(int j = 0; j < b.length; ++j) {
            dW[i][j] += a[i] * b[j];
         }
      }

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

   private double[][] xavier(int r, int c, Random rng) {
      double scale = Math.sqrt((double)2.0F / (double)(r + c));
      double[][] M = new double[r][c];

      for(int i = 0; i < r; ++i) {
         for(int j = 0; j < c; ++j) {
            M[i][j] = rng.nextGaussian() * scale;
         }
      }

      return M;
   }

   private double[] zeros1(int n) {
      return new double[n];
   }

   private double[][] zeros2(int r, int c) {
      return new double[r][c];
   }

   private double[] fill(int n, double v) {
      double[] a = new double[n];
      Arrays.fill(a, v);
      return a;
   }

   private void zeroMat(double[][] M) {
      for(double[] r : M) {
         Arrays.fill(r, (double)0.0F);
      }

   }

   private void zeroVec(double[] v) {
      Arrays.fill(v, (double)0.0F);
   }

   private double normMat(double[][] M) {
      double s = (double)0.0F;

      for(double[] r : M) {
         for(double x : r) {
            s += x * x;
         }
      }

      return s;
   }

   private double normVec(double[] v) {
      double s = (double)0.0F;

      for(double x : v) {
         s += x * x;
      }

      return s;
   }

   private void scaleMat(double[][] M, double s) {
      for(double[] r : M) {
         for(int j = 0; j < r.length; ++j) {
            r[j] *= s;
         }
      }

   }

   private void scaleVec(double[] v, double s) {
      for(int i = 0; i < v.length; ++i) {
         v[i] *= s;
      }

   }

   private JsonArray matToJson(double[][] M) {
      JsonArray a = new JsonArray();

      for(double[] r : M) {
         JsonArray row = new JsonArray();

         for(double x : r) {
            row.add(x);
         }

         a.add(row);
      }

      return a;
   }

   private JsonArray vecToJson(double[] v) {
      JsonArray a = new JsonArray();

      for(double x : v) {
         a.add(x);
      }

      return a;
   }

   private float wrapDeg(float d) {
      d %= 360.0F;
      if (d >= 180.0F) {
         d -= 360.0F;
      }

      if (d < -180.0F) {
         d += 360.0F;
      }

      return d;
   }

   private void shuffleArray(Integer[] arr) {
      Random rng = new Random();

      for(int i = arr.length - 1; i > 0; --i) {
         int j = rng.nextInt(i + 1);
         int tmp = arr[i];
         arr[i] = arr[j];
         arr[j] = tmp;
      }

   }
}
