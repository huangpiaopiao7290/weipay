package com.weipay.controller;

import com.weipay.entity.Product;
import com.weipay.service.ProductService;
import com.weipay.vo.R;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 商品接口
 */
@CrossOrigin
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Resource(name = "ProductServiceImpl")
    private ProductService productService;

    /**
     * 获取商品列表
     * @return
     */
    @GetMapping("/list")
    public R getProductList() {
        List<Product> productList = productService.list();
        return R.ok().data("productList", productList);
    }

}
