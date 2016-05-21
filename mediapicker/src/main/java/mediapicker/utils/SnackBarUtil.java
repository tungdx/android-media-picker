package mediapicker.utils;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

/**
 * Created by MrJiang on 2016-3-14-0014.
 */
public class SnackBarUtil {

  /**
   * @param container 容器View
   * @param text 内容文本
   * @param duration 显示时长
   * @param bgColor 背景色
   * @param textColor 文本颜色
   * @param actionColor 按钮颜色
   * @param maxLines 最大行数
   * @param actionText 按钮文本
   * @param listener 监听事件
   */
  public static void showText(View container, String text, int duration, String bgColor,
      String textColor, String actionColor, int maxLines, String actionText,
      View.OnClickListener listener) {

  }

  public static void showText(View container, String text, int duration) {
    Snackbar.make(container, text, duration).show();
  }

  public static void showText(Activity container, String text, int duration) {
    Snackbar.make(container.findViewById(android.R.id.content), text, duration).show();
  }

  public static void showText(Activity activity, String text) {
    Snackbar.make(activity.getWindow().getDecorView(), text, Snackbar.LENGTH_SHORT).show();
  }

  public static void showTextByToast(Activity activity, String text, int Position) {
    Toast toast = Toast.makeText(activity, text, Toast.LENGTH_SHORT);
    toast.setGravity(Position, 0, 0);
    toast.show();
  }
}
