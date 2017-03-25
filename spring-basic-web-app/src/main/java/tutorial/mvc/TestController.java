package tutorial.mvc;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
/**
 * Created by HuyNguyen on 3/25/17.
 */
@Controller
public class TestController {
    @RequestMapping("/test")
    public String test() {
        return "view";
    }
}
