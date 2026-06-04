# -*- coding: utf-8 -*-
import os

ROOT = os.path.join(os.path.dirname(__file__), '..', 'ruoyi-ui', 'src', 'views', 'shop')

layout_path = os.path.join(ROOT, 'layout.vue')
text = open(layout_path, encoding='utf-8').read()
text = text.replace('class="brand">???????</router-link>', 'class="brand">\u82e5\u4f9d\u5546\u57ce</router-link>')
text = text.replace('to="/shop/home" class="topnav-item" active-class="active">???</router-link>',
                    'to="/shop/home" class="topnav-item" active-class="active">\u9996\u9875</router-link>')
text = text.replace('to="/shop/cart" class="topnav-item" active-class="active">????</router-link>',
                    'to="/shop/cart" class="topnav-item" active-class="active">\u8d2d\u7269\u8f66</router-link>')
text = text.replace('to="/shop/orders" class="topnav-item" active-class="active">??????</router-link>',
                    'to="/shop/orders" class="topnav-item" active-class="active">\u6211\u7684\u8ba2\u5355</router-link>')
text = text.replace('v-if="!isLogin" to="/login" class="shop-login-link">???</router-link>',
                    'v-if="!isLogin" to="/login" class="shop-login-link">\u767b\u5f55</router-link>')
text = text.replace('v-else to="/index" class="shop-login-link">???????</router-link>',
                    'v-else to="/index" class="shop-login-link">\u7ba1\u7406\u540e\u53f0</router-link>')
text = text.replace('to="/shop/home" class="tab-item" active-class="active">\n        <el-icon><HomeFilled /></el-icon>\n        <span>???</span>',
                    'to="/shop/home" class="tab-item" active-class="active">\n        <el-icon><HomeFilled /></el-icon>\n        <span>\u9996\u9875</span>')
text = text.replace('to="/shop/cart" class="tab-item" active-class="active">\n        <el-icon><ShoppingCart /></el-icon>\n        <span>????</span>',
                    'to="/shop/cart" class="tab-item" active-class="active">\n        <el-icon><ShoppingCart /></el-icon>\n        <span>\u8d2d\u7269\u8f66</span>')
text = text.replace('to="/shop/orders" class="tab-item" active-class="active">\n        <el-icon><List /></el-icon>\n        <span>????</span>',
                    'to="/shop/orders" class="tab-item" active-class="active">\n        <el-icon><List /></el-icon>\n        <span>\u8ba2\u5355</span>')
text = text.replace("route.meta?.title || '???'", "route.meta?.title || '\u5546\u57ce'")
text = text.replace('max-width: 480px;', 'max-width: var(--shop-max-width, 480px);')
open(layout_path, 'w', encoding='utf-8', newline='\n').write(text)

product_path = os.path.join(ROOT, 'product.vue')
content = open(product_path, encoding='utf-8').read()
for a, b in [
    ('??{{ product.price }}', '\uffe5{{ product.price }}'),
    ('??? {{ product.stock }} ?? ???? {{ product.saleCount || 0 }}',
     '\u5e93\u5b58 {{ product.stock }} \u00b7 \u9500\u91cf {{ product.saleCount || 0 }}'),
    ("'????????'", "'\u6682\u65e0\u63cf\u8ff0'"),
    ('<span>????</span>', '<span>\u6570\u91cf</span>'),
    ('@click="addCart">??????</el-button>', '@click="addCart">\u52a0\u5165\u8d2d\u7269\u8f66</el-button>'),
    ('@click="buyNow">????????</el-button>', '@click="buyNow">\u7acb\u5373\u8d2d\u4e70</el-button>'),
    ("msgError('?????????')", "msgError('\u5546\u54c1\u4e0d\u5b58\u5728')"),
    ("msgSuccess('???????')", "msgSuccess('\u5df2\u52a0\u5165\u8d2d\u7269\u8f66')"),
]:
    content = content.replace(a, b)
open(product_path, 'w', encoding='utf-8', newline='\n').write(content)
print('done')
