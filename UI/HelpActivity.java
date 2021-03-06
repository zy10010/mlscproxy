package android.lovefantasy.mlscproxy.UI;

import android.lovefantasy.mlscproxy.Base.Core;
import android.lovefantasy.mlscproxy.Messages.MSG;
import android.lovefantasy.mlscproxy.Base.App;
import android.lovefantasy.mlscproxy.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class HelpActivity extends BaseActivity implements Handler.Callback {

    String help ="分为4个模块：global、http、https、httpdns\n" +
            "http和https模块的set_first del_hdr strrep regrep语法从上往下执行\n" +
            "set_first strrep regrep关键字: [M]，[H]，[U]，[url]，[V]，\\0，\\r，\\n，\\v，\\f，\\b，\\t，\\a。如果原本请求头含有关键字也会被替换\n" +
            "[M] 原请求方法\n" +
            "[H] 原请求host\n" +
            "[U] 原请求uri\n" +
            "[url] 原请求url\n" +
            "[V] 原请求协议版本\n" +
            "[0] \\0\n" +
            "\n" +
            "//全局模块\n" +
            "global {\n" +
            "    设置运行uid\n" +
            "    uid = 3004;\n" +
            "    \n" +
            "    http处理模式[wap wap_connect net_proxy net_connect] 不设置则为net\n" +
            "    wap: 所有请求走代理ip\n" +
            "    wap_connect: 所有请求走https代理ip\n" +
            "    net_proxy: HTTP请求80 8080端口直连目标服务器，其他端口走http代理ip\n" +
            "    net_connect : HTTP请求80 8080端口直连目标服务器，其他端口走https代理ip\n" +
            "    net: HTTP请求直连目标服务器\n" +
            "    mode = wap;\n" +
            "\n" +
            "    //TCP，DNS监听地址，不填IP则为默认IP\n" +
            "    tcp_listen = 10086;\n" +
            "    dns_listen = 10086;\n" +
            "\n" +
            "    //进程数\n" +
            "    procs = 2;\n" +
            "    \n" +
            "    //严格修改请求头，对于一次读取数据连续的多个请求头（比如qq浏览器加载\"看热点\"），全部修改，默认只修改第一个请求头\n" +
            "    strict = on;\n" +
            "}\n" +
            "\n" +
            "//http模块\n" +
            "http {\n" +
            "    //普通http请求只留GET POST联网\n" +
            "    only_get_post = on;\n" +
            "    \n" +
            "    // http目标地址\n" +
            "    addr = 10.0.0.172:80;\n" +
            "    \n" +
            "    //删除Host行，不区分大小写\n" +
            "    del_hdr = host;\n" +
            "    del_hdr = X-Online-Host;\n" +
            "    \n" +
            "    //如果搜索到以下字符串则进行https代理(net模式下无效)\n" +
            "    proxy_https_string = WebSocket;\n" +
            "    proxy_https_string = Upgrade:;\n" +
            "\n" +
            "    //设置首行\n" +
            "    set_first = \"[M] [U] [V]\\r\\n Host: rd.go.10086.cn\\r\\n\";\n" +
            "    //字符串替换，区分大小写\n" +
            "    //strrep = \"Host:\" -> \"Cloud:\";\n" +
            "    \n" +
            "    //正则表达式替换，不区分大小写\n" +
            "    //regrep = \"^Host:[^\\n]*\\n\" -> \"Meng: [H]\\r\\n\";\n" +
            "}\n" +
            "\n" +
            "//https模块，语法有addr、del_hdr、set_first、strrep、regrep\n" +
            "https {\n" +
            "    addr = 10.0.0.172:80;\n" +
            "    del_hdr = host;\n" +
            "    set_first = \"CONNECT /rd.go.10086.cn HTTP/1.1\\r\\nHost: [H]\\r\\n\";\n" +
            "}\n" +
            "\n" +
            "//httpDNS模块\n" +
            "httpdns {\n" +
            "    //http请求目标地址\n" +
            "    addr = 182.254.118.118;\n" +
            "    //缓存路径，关闭的时候不要加-9，否则缓存无法写入缓存\n" +
            "    //cachePath = dns.cache;\n" +
            "    //限制缓存数目\n" +
            "    //cacheLimit = 64;\n" +
            "    //http请求头，不设置则用http模块修改后的默认请求，[D]为查询的域名\n" +
            "    //http_req = \"[M] http://rd.go.10086.cn/d?dn=[D] [V]\\r\\nHost: rd.go.10086.cn\\r\\nConnection: close\\r\\n\";\n" +
            "}\n" +
            "\n" +
            "\n";
    TextView textView;
    Handler mHandler=null;
    Core mCoreHelp=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_help, null);
        setContentView(view);
        initStatusBar((AppBarLayout) findViewById(R.id.appbar_help),view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("获取帮助");
        toolbar.setNavigationIcon(R.drawable.ic_back);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                //startActivity(new Intent(App.getContext(),AboutActivity.class));
              //  finish();
            }
        });
        mHandler=new Handler(this);
        mCoreHelp= App.getCoreHelper();
        textView = (TextView) findViewById(R.id.tv_help);

        final List<Integer> flags = new ArrayList<>();
        final List<Integer> colors = new ArrayList<>();
        final List<String> regxs = new ArrayList<>();

        regxs.add("^\\s*\\w*\\s*(?=\\{)|\\}$|\\{$");
        regxs.add("^\\s*\\w+\\s*(?==)");
        regxs.add("(?<==|:)\\s*\\d+(?=;)");
        regxs.add("[\\d+.]{8,}");
        regxs.add("(?<=\")[\\s\\S]*?(?=;)");

        flags.add(Pattern.MULTILINE);
        flags.add(Pattern.MULTILINE);
        flags.add(null);
        flags.add(null);
        flags.add(null);

        colors.add(getResources().getColor(R.color.Yellow));
        colors.add(getResources().getColor(R.color.Cyan));
        colors.add(getResources().getColor(R.color.Violet));
        colors.add(getResources().getColor(R.color.Orange));
        colors.add(getResources().getColor(R.color.Green));
        final int cs[] = {0, 0, 0, 0,-1};
        final int ce[] = {0, 0, 0, 0,0};
        new Thread(new Runnable() {
            @Override
            public void run() {
                SpannableString spannableString = mCoreHelp.hightlight(help, regxs, flags, colors,cs,ce);
                Message message = mHandler.obtainMessage(222);
                message.obj=spannableString;
                mHandler.sendMessage(message);
            }
        }).start();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
      //  startActivity(new Intent(App.getContext(),AboutActivity.class));
       // overridePendingTransition(R.anim.anim_loadmain,R.anim.anim_exitactivity);
      //  finish();
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == MSG.REGX) {
            textView.setText((CharSequence) msg.obj);
        }
        return true;
    }
}
