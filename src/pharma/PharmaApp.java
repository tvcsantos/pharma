/*
 * PharmaApp.java
 */

package pharma;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.CannotRealizeException;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class PharmaApp extends SingleFrameApplication {

    public static final File SETTINGS_DIR =
            new File(AppUtils.USER_HOME, ".pharma");

    private Player player;
    private MediaLocator ml;
    private boolean playerInitialized;
    private Connection dbConn;
    private Statement stat;
    private Vector readLock;
    private Timer timer;
    private TimerTask task;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        if (!SETTINGS_DIR.exists()) SETTINGS_DIR.mkdir();
        ml = new MediaLocator("vfw://0");
        try {
            Class.forName("org.sqlite.JDBC");
            String sdir = SETTINGS_DIR.toString() + "/pharmadb.db";
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + sdir);
            execUpdate("CREATE TABLE IF NOT EXISTS Pharma ("
                    + " BARCODE NUMERIC NOT NULL,"
                    + " NAME VARCHAR(200) NOT NULL,"
                    + " EXPIRATION DATE NOT NULL,"
                    + " UNITS INTEGER NOT NULL DEFAULT 1,"
                    + " PRIMARY KEY (BARCODE, EXPIRATION)"
                    + ");");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PharmaApp.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(PharmaApp.class.getName())
                    .log(Level.SEVERE, null, ex);
            dbConn = null;
        }

        readLock = new Vector();

        timer = new Timer();

        task = new TimerTask() {

            @Override
            public void run() {
                synchronized(this) {
                    deallocatePlayer();
                }
            }
        };

        timer.schedule(task, 5000, 5000);
        
        show(new PharmaView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of PharmaApp
     */
    public static PharmaApp getApplication() {
        return Application.getInstance(PharmaApp.class);
    }

    public void initPlayer() throws IOException,
            NoPlayerException, CannotRealizeException {
        if (!playerInitialized) {
            player = Manager.createRealizedPlayer(ml);
            playerInitialized = true;
        }
    }

    private void execUpdate(String sql) throws SQLException {
        stat = dbConn.createStatement();
        stat.executeUpdate(sql);
        closeStat();
    }

    private ResultSet execQuery(String query) throws SQLException {
        stat = dbConn.createStatement();
        ResultSet res = stat.executeQuery(query);
        return res;
    }

    public ResultSet list() throws SQLException {
        return execQuery("SELECT * FROM Pharma;");
    }

    public ResultSet expired() throws SQLException {
        Date today = Calendar.getInstance().getTime();
        today.setMinutes(0);
        today.setSeconds(0);
        today.setHours(0);
        return execQuery("SELECT * FROM Pharma " +
                "WHERE EXPIRATION <= " + today.getTime());
    }

    public int expiredSize() throws SQLException {
        Date today = Calendar.getInstance().getTime();
        today.setMinutes(0);
        today.setSeconds(0);
        today.setHours(0);
        ResultSet res = execQuery("SELECT SUM(UNITS) FROM (SELECT UNITS FROM Pharma " +
                "WHERE EXPIRATION <= " + today.getTime() + ")");
        /*ResultSet res = execQuery("SELECT COUNT(*) FROM Pharma " +
                "WHERE EXPIRATION <= " + today.getTime());*/
        res.next();
        int result = res.getInt(1);
        closeStat();
        return result;
    }

    public void closeStat() throws SQLException {
        if (stat != null) stat.close();
        stat = null;
    }

    public ResultSet select(long barcode) throws SQLException {
        stat = dbConn.createStatement();
        ResultSet res = stat.executeQuery(
                "SELECT * FROM Pharma WHERE BARCODE=" + barcode + ";");
        return res;
    }

    public ResultSet select(long barcode, Date exp) throws SQLException {
        if (exp == null) return select(barcode);
        stat = dbConn.createStatement();
        ResultSet res = stat.executeQuery(
                "SELECT * FROM Pharma WHERE BARCODE=" + barcode +
                " and EXPIRATION=" + exp.getTime() + ";");
        return res;
    }

    public boolean delete(long barcode, Date exp) throws SQLException {
        stat = dbConn.createStatement();
        ResultSet rs = stat.executeQuery(
                "SELECT * FROM Pharma WHERE BARCODE=" + barcode +
                " and EXPIRATION=" + exp.getTime() + ";");
        if (!rs.next()) {
            closeStat();
            return false;
        } else {
            int units = rs.getInt(4);
            closeStat();
             stat = dbConn.createStatement();
            if (units > 1) {
                stat.executeUpdate("UPDATE Pharma SET UNITS = UNITS - 1 " +
                    "WHERE BARCODE=" + barcode +
                    " and EXPIRATION=" + exp.getTime() + ";");
            } else {
                 stat.executeUpdate("DELETE FROM Pharma " +
                    "WHERE BARCODE=" + barcode +
                    " and EXPIRATION=" + exp.getTime() + ";");
            }
            closeStat();
            return true;
        }
    }

    public boolean hasCode(long barcode, Date exp) throws SQLException {
        stat = dbConn.createStatement();
        ResultSet rs = stat.executeQuery(
                "SELECT COUNT(*) FROM Pharma WHERE BARCODE=" + barcode +
                " and EXPIRATION=" + exp.getTime() + ";");
        rs.next();
        int count = rs.getInt(1);
        closeStat();
        if (count <= 0) return false;
        else return true;
    }

    public boolean update(long barcode, Date exp) throws SQLException {
        stat = dbConn.createStatement();
        ResultSet rs = stat.executeQuery(
                "SELECT COUNT(*) FROM Pharma WHERE BARCODE=" + barcode +
                " and EXPIRATION=" + exp.getTime() + ";");
        rs.next();
        int count = rs.getInt(1);
        closeStat();
        if (count <= 0) { //must insert
            return false;
        } else {
            stat = dbConn.createStatement();
            stat.executeUpdate("UPDATE Pharma SET UNITS = UNITS + 1 " +
                    "WHERE BARCODE=" + barcode +
                    " and EXPIRATION=" + exp.getTime() + ";");
            closeStat();
            return true;
        }
    }

    public boolean update(long barcode, Date exp, int units)
            throws SQLException {
        stat = dbConn.createStatement();
        ResultSet rs = stat.executeQuery(
                "SELECT COUNT(*) FROM Pharma WHERE BARCODE=" + barcode +
                " and EXPIRATION=" + exp.getTime() + ";");
        rs.next();
        int count = rs.getInt(1);
        closeStat();
        if (count <= 0) { //must insert
            return false;
        } else {
            stat = dbConn.createStatement();
            stat.executeUpdate("UPDATE Pharma SET UNITS =" + units +
                    " WHERE BARCODE=" + barcode +
                    " and EXPIRATION=" + exp.getTime() + ";");
            closeStat();
            return true;
        }
    }

    public void insert(long barcode, String name, Date exp, int units)
            throws SQLException {
        stat = dbConn.createStatement();
        stat.executeUpdate("INSERT INTO Pharma " +
                "(BARCODE, NAME, EXPIRATION, UNITS)" +
            "VALUES(" + barcode + ",\"" + name + "\"" +
            "," + exp.getTime() + "," + units + ")");
	closeStat();
    }

    public void insert(long barcode, String name, Date exp)
            throws SQLException {
        stat = dbConn.createStatement();
        stat.executeUpdate("INSERT INTO Pharma " +
                "(BARCODE, NAME, EXPIRATION)" +
            "VALUES(" + barcode + ",\"" + name + "\"" +
            "," + exp.getTime() + ")");
	closeStat();
    }

    public void startPlayer() {
        player.start();
    }

    public void stopPlayer() {
        player.stop();
    }

    public void obtainRL(Object o) {
        if (!readLock.contains(o))
            readLock.add(o);
    }

    public void freeRL(Object o) {
        readLock.remove(o);
    }

    private synchronized void deallocatePlayer() {
        if (playerInitialized && readLock.isEmpty()) {
            player.stop();
            player.deallocate();
            player.close();
            playerInitialized = false;
        }
    }

    public Component getPlayerVisualComponent() {
        return player.getVisualComponent();
    }

    public BufferedImage grabFrame() {
        FrameGrabbingControl fgc = (FrameGrabbingControl)
                player.getControl("javax.media.control.FrameGrabbingControl");
        Buffer buf = fgc.grabFrame();

        // Convert it to an image
        BufferToImage btoi = new BufferToImage((VideoFormat) buf.getFormat());
        Image img = btoi.createImage(buf);

        BufferedImage bi = new BufferedImage(img.getWidth(null),
                img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bi.createGraphics();
        g2.drawImage(img, null, null);
        return bi;
    }

    private static final String getDecodeTextT(BufferedImage image)
            throws InvalidParameterException, NotFoundException
    {
        if (image == null) {
            throw new InvalidParameterException("Image cannot be null");
        }

        //saveImage(image, "test" + (testRot++) + ".png");

        for (int i = 0 ; i < 4; i++) {
            try {
                LuminanceSource source =
                        new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap =
                        new BinaryBitmap(new HybridBinarizer(source));
                Result result = new MultiFormatReader().decode(bitmap);
                return result.getText();
            } catch(NotFoundException ex) {
                image = rotate90(image);
                if (i == 3) throw ex;
                continue;
            }
        }
        return null;
    }
    
    public static final String getDecodeText(BufferedImage image)
            throws InvalidParameterException, NotFoundException
    {
        /*if (image == null) {
            throw new InvalidParameterException("Image cannot be null");
        }

        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();*/
        return getDecodeTextT(image);
    }

    private static AffineTransform findTranslation(AffineTransform at,
            BufferedImage bi) {
        Point2D p2din, p2dout;

        p2din = new Point2D.Double(0.0, 0.0);
        p2dout = at.transform(p2din, null);
        double ytrans = p2dout.getY();

        p2din = new Point2D.Double(0, bi.getHeight());
        p2dout = at.transform(p2din, null);
        double xtrans = p2dout.getX();

        AffineTransform tat = new AffineTransform();
        tat.translate(-xtrans, -ytrans);
        return tat;
    }

    private static BufferedImage rotate90(BufferedImage img) {

        AffineTransform at = new AffineTransform();

        int w = img.getWidth();
        int h = img.getHeight();
        at.rotate(Math.toRadians(90), w / 2.0, h / 2.0);

        AffineTransform translationTransform;
        translationTransform = findTranslation(at, img);
        at.preConcatenate(translationTransform);

        AffineTransformOp op = new AffineTransformOp(at,
                AffineTransformOp.TYPE_BILINEAR);
        //BufferedImage dimg = new BufferedImage(h, w, img.getType());
        BufferedImage dimg = op.filter(img, null);
        //saveImage(dimg, "test" + (testRot++) + ".png");
        return dimg;
    }

    //public static int testRot = 1;

    private static void saveImage(BufferedImage img, String ref) {
        try {
            String format = (ref.endsWith(".png")) ? "png" : "jpg";
            ImageIO.write(img, format, new File(ref));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
                org.jdesktop.application.SessionStorage.class.getName());
        logger.setLevel(java.util.logging.Level.OFF);
        launch(PharmaApp.class, args);
    }
}
