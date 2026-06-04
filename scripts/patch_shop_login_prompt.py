# -*- coding: utf-8 -*-
import os

VIEWS = os.path.join(os.path.dirname(__file__), '..', 'ruoyi-ui', 'src', 'views', 'shop')


def patch_layout2():
    path = os.path.join(VIEWS, 'layout.vue')
    t = open(path, encoding='utf-8').read()
    if 'shopNavClick' not in t:
        t = t.replace(
            "import { getToken } from '@/utils/auth'\nimport useUserStore",
            "import { getToken } from '@/utils/auth'\nimport { shopNavClick } from '@/utils/shopAuth'\nimport useUserStore"
        )
    cart_old = 'to="/shop/cart" class="topnav-item" active-class="active">'
    cart_new = (
        'to="/shop/cart" custom v-slot="{ navigate, isActive }">\n'
        '            <span class="topnav-item" :class="{ active: isActive }" '
        '@click="shopNavClick(router, \'/shop/cart\', navigate)">'
    )
    if cart_old in t:
        idx = t.find(cart_old)
        end = t.find('</router-link>', idx)
        inner = t[idx:end]
        label_end = inner.find('>') + 1
        label = inner[label_end:].replace('</router-link>', '')
        t = t[:idx] + cart_new + label + '</span>\n          </router-link>' + t[end + len('</router-link>'):]

    ord_old = 'to="/shop/orders" class="topnav-item" active-class="active">'
    ord_new = (
        'to="/shop/orders" custom v-slot="{ navigate, isActive }">\n'
        '            <span class="topnav-item" :class="{ active: isActive }" '
        '@click="shopNavClick(router, \'/shop/orders\', navigate)">'
    )
    if ord_old in t:
        idx = t.find(ord_old)
        end = t.find('</router-link>', idx)
        inner = t[idx:end]
        label_end = inner.find('>') + 1
        label = inner[label_end:].replace('</router-link>', '')
        t = t[:idx] + ord_new + label + '</span>\n          </router-link>' + t[end + len('</router-link>'):]

    tab_cart = 'to="/shop/cart" class="tab-item" active-class="active">'
    if tab_cart in t:
        idx = t.find(tab_cart)
        end = t.find('</router-link>', idx)
        new_block = (
            'to="/shop/cart" custom v-slot="{ navigate, isActive }">\n'
            '          <span class="tab-item" :class="{ active: isActive }" '
            '@click="shopNavClick(router, \'/shop/cart\', navigate)">\n'
            '            <el-icon><ShoppingCart /></el-icon><span>'
            + '\u8d2d\u7269\u8f66'
            + '</span>\n          </span>\n        </router-link>'
        )
        t = t[:idx] + new_block + t[end + len('</router-link>'):]

    tab_ord = 'to="/shop/orders" class="tab-item" active-class="active">'
    if tab_ord in t:
        idx = t.find(tab_ord)
        end = t.find('</router-link>', idx)
        new_block = (
            'to="/shop/orders" custom v-slot="{ navigate, isActive }">\n'
            '          <span class="tab-item" :class="{ active: isActive }" '
            '@click="shopNavClick(router, \'/shop/orders\', navigate)">\n'
            '            <el-icon><List /></el-icon><span>'
            + '\u8ba2\u5355'
            + '</span>\n          </span>\n        </router-link>'
        )
        t = t[:idx] + new_block + t[end + len('</router-link>'):]

    open(path, 'w', encoding='utf-8', newline='\n').write(t)
    print('layout ok')


def apply(path, pairs):
    t = open(path, encoding='utf-8').read()
    for a, b in pairs:
        if not a or a not in t:
            continue
        if 'function goLogin' in b and 'function goLogin' in t:
            continue
        if b.count('const isLogin') and t.count('const isLogin') >= 1:
            continue
        t = t.replace(a, b, 1)
    open(path, 'w', encoding='utf-8', newline='\n').write(t)


patch_layout2()

apply(os.path.join(VIEWS, 'cart.vue'), [
    ('description="\u6682\u65e0\u5546\u54c1_CART"', 'description="\u8d2d\u7269\u8f66\u662f\u7a7a\u7684"'),
    (
        "import { listCart, updateCartChecked, updateCartQuantity, removeCart } from '@/api/app/cart'\nconst router",
        "import { listCart, updateCartChecked, updateCartQuantity, removeCart } from '@/api/app/cart'\n"
        "import { getToken } from '@/utils/auth'\nimport { promptShopLogin } from '@/utils/shopAuth'\nconst router",
    ),
    (
        'const cartList = ref([])\nconst loading = ref(true)',
        'const cartList = ref([])\nconst loading = ref(true)\nconst isLogin = computed(() => !!getToken())\n'
        "function goLogin() { promptShopLogin(router, '/shop/cart', { scene: 'cart' }) }",
    ),
    (
        '<div v-loading="loading" class="cart-list">',
        '<el-empty v-if="!isLogin" description="\u767b\u5f55\u540e\u67e5\u770b\u548c\u7ba1\u7406\u8d2d\u7269\u8f66" class="guest-empty">\n'
        '      <el-button type="primary" @click="goLogin">\u53bb\u767b\u5f55</el-button>\n'
        '      <el-button @click="router.push(\'/shop/home\')">\u53bb\u901b\u901b</el-button>\n'
        '    </el-empty>\n    <div v-else v-loading="loading" class="cart-list">',
    ),
    (
        'function loadCart() {\n  loading.value = true',
        'function loadCart() {\n  if (!getToken()) { loading.value = false; return }\n  loading.value = true',
    ),
    (
        'function goCheckout() {\n  const ids =',
        "function goCheckout() {\n  if (!getToken()) { promptShopLogin(router, '/shop/checkout', { scene: 'checkout' }); return }\n  const ids =",
    ),
    ('v-if="cartList.length">', 'v-if="isLogin && cartList.length">'),
])
t = open(os.path.join(VIEWS, 'cart.vue'), encoding='utf-8').read()
if '.guest-empty' not in t:
    apply(os.path.join(VIEWS, 'cart.vue'), [('</style>', '.guest-empty { padding: 48px 16px; }\n</style>')])
print('cart ok')

apply(os.path.join(VIEWS, 'orders.vue'), [
    (
        "import { listMyOrders, payOrder, cancelMyOrder } from '@/api/app/order'\nconst router",
        "import { listMyOrders, payOrder, cancelMyOrder } from '@/api/app/order'\n"
        "import { getToken } from '@/utils/auth'\nimport { promptShopLogin } from '@/utils/shopAuth'\nconst router",
    ),
    (
        'const orders = ref([])\nconst loading = ref(true)',
        'const orders = ref([])\nconst loading = ref(true)\nconst isLogin = computed(() => !!getToken())\n'
        "function goLogin() { promptShopLogin(router, '/shop/orders', { scene: 'orders' }) }",
    ),
    (
        '<div v-loading="loading">',
        '<el-empty v-if="!isLogin" description="\u767b\u5f55\u540e\u67e5\u770b\u8ba2\u5355\u4e0e\u7269\u6d41\u72b6\u6001" class="guest-empty">\n'
        '      <el-button type="primary" @click="goLogin">\u53bb\u767b\u5f55</el-button>\n'
        '    </el-empty>\n    <div v-else v-loading="loading">',
    ),
    (
        'function loadOrders() {\n  loading.value = true',
        'function loadOrders() {\n  if (!getToken()) { loading.value = false; return }\n  loading.value = true',
    ),
])
t = open(os.path.join(VIEWS, 'orders.vue'), encoding='utf-8').read()
t = t.replace("'\u53d6\u6d88_ASK'", "'\u786e\u8ba4\u53d6\u6d88\u8be5\u8ba2\u5355\uff1f'")
t = t.replace("'\u53d6\u6d88ED'", "'\u5df2\u53d6\u6d88'")
if '.guest-empty' not in t:
    t = t.replace('</style>', '.guest-empty { padding: 48px 16px; }\n</style>')
open(os.path.join(VIEWS, 'orders.vue'), 'w', encoding='utf-8', newline='\n').write(t)
print('orders ok')

apply(os.path.join(VIEWS, 'checkout.vue'), [
    ('label="\u6536\u8d27\u4fe1\u606f_NAME"', 'label="\u6536\u8d27\u4eba"'),
    (
        "import { checkoutOrder, payOrder } from '@/api/app/order'\nconst route",
        "import { checkoutOrder, payOrder } from '@/api/app/order'\n"
        "import { getToken } from '@/utils/auth'\nimport { promptShopLogin } from '@/utils/shopAuth'\nconst route",
    ),
    (
        'const submitting = ref(false)',
        'const submitting = ref(false)\nconst isLogin = computed(() => !!getToken())\n'
        "function goLogin() { promptShopLogin(router, '/shop/checkout', { scene: 'checkout' }) }",
    ),
    (
        '<el-form ref="formRef"',
        '<el-empty v-if="!isLogin" description="\u8bf7\u5148\u767b\u5f55\u540e\u518d\u7ed3\u7b97\u8ba2\u5355" class="guest-empty">\n'
        '      <el-button type="primary" @click="goLogin">\u53bb\u767b\u5f55</el-button>\n'
        '    </el-empty>\n    <template v-else>\n    <el-form ref="formRef"',
    ),
    (
        '    <div class="submit-panel shop-only-pc">\n      <el-button type="danger" size="large" :loading="submitting" @click="submitOrder">\u63d0\u4ea4\u8ba2\u5355</el-button>\n    </div>\n  </div>\n</template>',
        '    <div class="submit-panel shop-only-pc">\n      <el-button type="danger" size="large" :loading="submitting" @click="submitOrder">\u63d0\u4ea4\u8ba2\u5355</el-button>\n    </div>\n    </template>\n  </div>\n</template>',
    ),
])
t = open(os.path.join(VIEWS, 'checkout.vue'), encoding='utf-8').read()
if '.guest-empty' not in t:
    t = t.replace('</style>', '.guest-empty { padding: 48px 16px; }\n</style>')
    open(os.path.join(VIEWS, 'checkout.vue'), 'w', encoding='utf-8', newline='\n').write(t)
print('checkout ok')

apply(os.path.join(VIEWS, 'order-detail.vue'), [
    (
        "import { getMyOrder, payOrder, cancelMyOrder } from '@/api/app/order'\nconst route",
        "import { getMyOrder, payOrder, cancelMyOrder } from '@/api/app/order'\n"
        "import { getToken } from '@/utils/auth'\nimport { promptShopLogin } from '@/utils/shopAuth'\nconst route",
    ),
    (
        'const order = ref(null)\nconst loading = ref(true)',
        'const order = ref(null)\nconst loading = ref(true)\nconst isLogin = computed(() => !!getToken())\n'
        "function goLogin() { promptShopLogin(router, route.fullPath, { scene: 'orders' }) }",
    ),
    (
        '<template v-if="order">',
        '<el-empty v-if="!isLogin" description="\u767b\u5f55\u540e\u67e5\u770b\u8ba2\u5355\u8be6\u60c5" class="guest-empty">\n'
        '      <el-button type="primary" @click="goLogin">\u53bb\u767b\u5f55</el-button>\n'
        '    </el-empty>\n    <template v-else-if="order">',
    ),
    (
        'function load() {\n  getMyOrder',
        'function load() {\n  if (!getToken()) { loading.value = false; return }\n  getMyOrder',
    ),
])
t = open(os.path.join(VIEWS, 'order-detail.vue'), encoding='utf-8').read()
t = t.replace("'\u53d6\u6d88_ASK'", "'\u786e\u8ba4\u53d6\u6d88\u8be5\u8ba2\u5355\uff1f'")
t = t.replace("'\u53d6\u6d88ED'", "'\u5df2\u53d6\u6d88'")
if '.guest-empty' not in t:
    t = t.replace('</style>', '.guest-empty { padding: 48px 16px; }\n</style>')
open(os.path.join(VIEWS, 'order-detail.vue'), 'w', encoding='utf-8', newline='\n').write(t)
print('order-detail ok')
