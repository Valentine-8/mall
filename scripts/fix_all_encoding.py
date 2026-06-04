# -*- coding: utf-8 -*-
"""Scan and fix GBK/mojibake encoding issues across mall project."""
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SCRIPTS = os.path.join(ROOT, 'scripts')
UI = os.path.join(ROOT, 'ruoyi-ui')
SHOP = os.path.join(UI, 'src', 'views', 'shop')

# --- Java message constants (ASCII-safe unicode escapes) ---
J = {
    'STOCK': '\u5e93\u5b58\u4e0d\u8db3',
    'QTY_MIN': '\u6570\u91cf\u81f3\u5c11\u4e3a1',
    'CART_MISSING': '\u8d2d\u7269\u8f66\u8bb0\u5f55\u4e0d\u5b58\u5728',
    'PRODUCT_OFF': '\u5546\u54c1\u4e0d\u5b58\u5728\u6216\u5df2\u4e0b\u67b6',
    'NO_SETTLE': '\u8bf7\u5148\u52fe\u9009\u8981\u7ed3\u7b97\u7684\u5546\u54c1',
    'OFF_SALE': '\u5546\u54c1\u5df2\u4e0b\u67b6\uff1a',
    'LOW_STOCK': '\u5e93\u5b58\u4e0d\u8db3\uff1a',
    'DEDUCT_FAIL': '\u6263\u51cf\u5e93\u5b58\u5931\u8d25\uff1a',
    'ORDER_MISSING': '\u8ba2\u5355\u4e0d\u5b58\u5728',
    'ORDER_FORBID': '\u65e0\u6743\u64cd\u4f5c\u8be5\u8ba2\u5355',
    'ORDER_DONE': '\u8ba2\u5355\u5df2\u5b8c\u6210\u6216\u5df2\u53d6\u6d88',
    'ORDER_CANCEL_RULE': '\u5f53\u524d\u72b6\u6001\u4e0d\u5141\u8bb8\u53d6\u6d88',
    'ORDER_PAY_ONLY': '\u4ec5\u5f85\u4ed8\u6b3e\u8ba2\u5355\u53ef\u652f\u4ed8',
    'SHIP_PAID': '\u4ec5\u5df2\u4ed8\u6b3e\u8ba2\u5355\u53ef\u53d1\u8d27',
    'FINISH_SHIPPED': '\u4ec5\u5df2\u53d1\u8d27\u8ba2\u5355\u53ef\u5b8c\u6210',
    'ADMIN_CANCEL': '\u8ba2\u5355\u5df2\u5b8c\u6210\u6216\u5df2\u53d6\u6d88',
}


def w(rel, content):
    p = os.path.join(ROOT, rel.replace('/', os.sep))
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, 'w', encoding='utf-8', newline='\n') as f:
        f.write(content)
    print('wrote', rel)


def fix_mall_cart_service():
    w('ruoyi-mall/src/main/java/com/ruoyi/mall/service/impl/MallCartServiceImpl.java', f'''package com.ruoyi.mall.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.domain.MallCart;
import com.ruoyi.mall.domain.MallProduct;
import com.ruoyi.mall.domain.vo.MallCartVo;
import com.ruoyi.mall.mapper.MallCartMapper;
import com.ruoyi.mall.mapper.MallProductMapper;
import com.ruoyi.mall.service.IMallCartService;

@Service
public class MallCartServiceImpl implements IMallCartService
{{
    private static final String MSG_STOCK = "{J['STOCK']}";
    private static final String MSG_QTY = "{J['QTY_MIN']}";
    private static final String MSG_CART = "{J['CART_MISSING']}";
    private static final String MSG_PRODUCT = "{J['PRODUCT_OFF']}";

    @Autowired
    private MallCartMapper cartMapper;
    @Autowired
    private MallProductMapper productMapper;

    @Override
    public List<MallCartVo> selectCartList(Long userId)
    {{
        return cartMapper.selectCartVoListByUserId(userId);
    }}

    @Override
    public int addToCart(Long userId, Long productId, Integer quantity)
    {{
        MallProduct product = requireOnSaleProduct(productId);
        if (quantity == null || quantity < 1) quantity = 1;
        if (product.getStock() < quantity) throw new ServiceException(MSG_STOCK);
        MallCart exist = cartMapper.selectCartByUserAndProduct(userId, productId);
        if (exist != null)
        {{
            int newQty = exist.getQuantity() + quantity;
            if (product.getStock() < newQty) throw new ServiceException(MSG_STOCK);
            exist.setQuantity(newQty);
            exist.setChecked("1");
            return cartMapper.updateMallCart(exist);
        }}
        MallCart cart = new MallCart();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setQuantity(quantity);
        cart.setChecked("1");
        return cartMapper.insertMallCart(cart);
    }}

    @Override
    public int updateQuantity(Long userId, Long cartId, Integer quantity)
    {{
        MallCart cart = requireOwnCart(userId, cartId);
        MallProduct product = requireOnSaleProduct(cart.getProductId());
        if (quantity == null || quantity < 1) throw new ServiceException(MSG_QTY);
        if (product.getStock() < quantity) throw new ServiceException(MSG_STOCK);
        cart.setQuantity(quantity);
        return cartMapper.updateMallCart(cart);
    }}

    @Override
    public int updateChecked(Long userId, Long cartId, String checked)
    {{
        MallCart cart = requireOwnCart(userId, cartId);
        cart.setChecked("1".equals(checked) ? "1" : "0");
        return cartMapper.updateMallCart(cart);
    }}

    @Override
    public int removeCart(Long userId, Long[] cartIds)
    {{
        if (cartIds == null || cartIds.length == 0) return 0;
        return cartMapper.deleteCartByIds(userId, cartIds);
    }}

    private MallCart requireOwnCart(Long userId, Long cartId)
    {{
        MallCart cart = cartMapper.selectCartById(cartId);
        if (cart == null || !userId.equals(cart.getUserId()))
            throw new ServiceException(MSG_CART);
        return cart;
    }}

    private MallProduct requireOnSaleProduct(Long productId)
    {{
        MallProduct product = productMapper.selectMallProductById(productId);
        if (product == null || !"0".equals(product.getStatus()))
            throw new ServiceException(MSG_PRODUCT);
        return product;
    }}
}}
''')


def fix_mall_order_service():
    w('ruoyi-mall/src/main/java/com/ruoyi/mall/service/impl/MallOrderServiceImpl.java', f'''package com.ruoyi.mall.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.domain.MallOrder;
import com.ruoyi.mall.mapper.MallOrderItemMapper;
import com.ruoyi.mall.mapper.MallOrderMapper;
import com.ruoyi.mall.service.IMallOrderService;

@Service
public class MallOrderServiceImpl implements IMallOrderService
{{
    private static final String MSG_SHIP = "{J['SHIP_PAID']}";
    private static final String MSG_FINISH = "{J['FINISH_SHIPPED']}";
    private static final String MSG_CANCEL = "{J['ADMIN_CANCEL']}";
    private static final String MSG_MISSING = "{J['ORDER_MISSING']}";

    @Autowired
    private MallOrderMapper orderMapper;
    @Autowired
    private MallOrderItemMapper orderItemMapper;

    @Override
    public MallOrder selectMallOrderById(Long orderId)
    {{
        MallOrder order = orderMapper.selectMallOrderById(orderId);
        if (order != null)
            order.setItems(orderItemMapper.selectMallOrderItemByOrderId(orderId));
        return order;
    }}

    @Override
    public List<MallOrder> selectMallOrderList(MallOrder order)
    {{
        return orderMapper.selectMallOrderList(order);
    }}

    @Override
    public int shipOrder(Long orderId, String updateBy)
    {{
        MallOrder order = requireOrder(orderId);
        if (!"1".equals(order.getStatus())) throw new ServiceException(MSG_SHIP);
        MallOrder update = new MallOrder();
        update.setOrderId(orderId);
        update.setStatus("2");
        update.setDeliveryTime(new Date());
        update.setUpdateBy(updateBy);
        return orderMapper.updateMallOrder(update);
    }}

    @Override
    public int finishOrder(Long orderId, String updateBy)
    {{
        MallOrder order = requireOrder(orderId);
        if (!"2".equals(order.getStatus())) throw new ServiceException(MSG_FINISH);
        MallOrder update = new MallOrder();
        update.setOrderId(orderId);
        update.setStatus("3");
        update.setFinishTime(new Date());
        update.setUpdateBy(updateBy);
        return orderMapper.updateMallOrder(update);
    }}

    @Override
    public int cancelOrder(Long orderId, String updateBy)
    {{
        MallOrder order = requireOrder(orderId);
        if ("3".equals(order.getStatus()) || "4".equals(order.getStatus()))
            throw new ServiceException(MSG_CANCEL);
        MallOrder update = new MallOrder();
        update.setOrderId(orderId);
        update.setStatus("4");
        update.setCancelTime(new Date());
        update.setUpdateBy(updateBy);
        return orderMapper.updateMallOrder(update);
    }}

    @Override
    @Transactional
    public int deleteMallOrderByIds(Long[] orderIds)
    {{
        orderItemMapper.deleteMallOrderItemByOrderIds(orderIds);
        return orderMapper.deleteMallOrderByIds(orderIds);
    }}

    private MallOrder requireOrder(Long orderId)
    {{
        MallOrder order = orderMapper.selectMallOrderById(orderId);
        if (order == null) throw new ServiceException(MSG_MISSING);
        return order;
    }}
}}
''')


def fix_mall_app_order_service():
    w('ruoyi-mall/src/main/java/com/ruoyi/mall/service/impl/MallAppOrderServiceImpl.java', f'''package com.ruoyi.mall.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.domain.MallOrder;
import com.ruoyi.mall.domain.MallOrderItem;
import com.ruoyi.mall.domain.dto.MallCheckoutDto;
import com.ruoyi.mall.domain.vo.MallCartVo;
import com.ruoyi.mall.mapper.MallCartMapper;
import com.ruoyi.mall.mapper.MallOrderItemMapper;
import com.ruoyi.mall.mapper.MallOrderMapper;
import com.ruoyi.mall.mapper.MallProductMapper;
import com.ruoyi.mall.service.IMallAppOrderService;
import com.ruoyi.mall.service.IMallCartService;

@Service
public class MallAppOrderServiceImpl implements IMallAppOrderService
{{
    private static final String MSG_NO_SETTLE = "{J['NO_SETTLE']}";
    private static final String MSG_OFF = "{J['OFF_SALE']}";
    private static final String MSG_LOW = "{J['LOW_STOCK']}";
    private static final String MSG_DEDUCT = "{J['DEDUCT_FAIL']}";
    private static final String MSG_MISSING = "{J['ORDER_MISSING']}";
    private static final String MSG_FORBID = "{J['ORDER_FORBID']}";
    private static final String MSG_DONE = "{J['ORDER_DONE']}";
    private static final String MSG_CANCEL_RULE = "{J['ORDER_CANCEL_RULE']}";
    private static final String MSG_PAY_ONLY = "{J['ORDER_PAY_ONLY']}";

    @Autowired
    private MallOrderMapper orderMapper;
    @Autowired
    private MallOrderItemMapper orderItemMapper;
    @Autowired
    private MallProductMapper productMapper;
    @Autowired
    private MallCartMapper cartMapper;
    @Autowired
    private IMallCartService cartService;

    @Override
    @Transactional
    public MallOrder createOrderFromCart(Long userId, String userName, MallCheckoutDto checkout)
    {{
        List<MallCartVo> settleList = filterSettleCarts(cartService.selectCartList(userId), checkout.getCartIds());
        if (settleList.isEmpty()) throw new ServiceException(MSG_NO_SETTLE);
        BigDecimal total = BigDecimal.ZERO;
        List<MallOrderItem> items = new ArrayList<>();
        List<Long> cartIdsToRemove = new ArrayList<>();
        for (MallCartVo cart : settleList)
        {{
            if (!"0".equals(cart.getStatus())) throw new ServiceException(MSG_OFF + cart.getProductName());
            if (cart.getStock() < cart.getQuantity()) throw new ServiceException(MSG_LOW + cart.getProductName());
            BigDecimal lineTotal = cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            total = total.add(lineTotal);
            MallOrderItem item = new MallOrderItem();
            item.setProductId(cart.getProductId());
            item.setProductName(cart.getProductName());
            item.setProductPic(cart.getPic());
            item.setProductPrice(cart.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setTotalAmount(lineTotal);
            items.add(item);
            cartIdsToRemove.add(cart.getCartId());
        }}
        MallOrder order = new MallOrder();
        order.setOrderSn(generateOrderSn());
        order.setUserId(userId);
        order.setUserName(userName);
        order.setTotalAmount(total);
        order.setPayAmount(total);
        order.setStatus("0");
        order.setReceiverName(checkout.getReceiverName());
        order.setReceiverPhone(checkout.getReceiverPhone());
        order.setReceiverAddress(checkout.getReceiverAddress());
        order.setRemark(checkout.getRemark());
        order.setCreateBy(userName);
        orderMapper.insertMallOrder(order);
        for (MallOrderItem item : items)
        {{
            item.setOrderId(order.getOrderId());
            if (productMapper.decreaseStock(item.getProductId(), item.getQuantity()) == 0)
                throw new ServiceException(MSG_DEDUCT + item.getProductName());
            productMapper.increaseSaleCount(item.getProductId(), item.getQuantity());
        }}
        orderItemMapper.batchInsertMallOrderItem(items);
        cartMapper.deleteCartByIds(userId, cartIdsToRemove.toArray(new Long[0]));
        order.setItems(items);
        return order;
    }}

    @Override
    public int payOrder(Long userId, Long orderId) {{
        MallOrder order = requireOwnPendingOrder(userId, orderId);
        MallOrder update = new MallOrder();
        update.setOrderId(orderId);
        update.setStatus("1");
        update.setPayTime(new Date());
        update.setUpdateBy(order.getUserName());
        return orderMapper.updateMallOrder(update);
    }}

    @Override
    public List<MallOrder> selectMyOrderList(Long userId) {{
        return orderMapper.selectMallOrderListByUserId(userId);
    }}

    @Override
    public MallOrder selectMyOrderDetail(Long userId, Long orderId) {{
        MallOrder order = orderMapper.selectMallOrderById(orderId);
        if (order == null || !userId.equals(order.getUserId())) throw new ServiceException(MSG_MISSING);
        order.setItems(orderItemMapper.selectMallOrderItemByOrderId(orderId));
        return order;
    }}

    @Override
    public int cancelMyOrder(Long userId, Long orderId) {{
        MallOrder order = orderMapper.selectMallOrderById(orderId);
        if (order == null || !userId.equals(order.getUserId())) throw new ServiceException(MSG_MISSING);
        if ("3".equals(order.getStatus()) || "4".equals(order.getStatus())) throw new ServiceException(MSG_DONE);
        if ("0".equals(order.getStatus())) {{
            for (MallOrderItem item : orderItemMapper.selectMallOrderItemByOrderId(orderId))
                productMapper.restoreStock(item.getProductId(), item.getQuantity());
            MallOrder update = new MallOrder();
            update.setOrderId(orderId);
            update.setStatus("4");
            update.setCancelTime(new Date());
            return orderMapper.updateMallOrder(update);
        }}
        throw new ServiceException(MSG_CANCEL_RULE);
    }}

    private List<MallCartVo> filterSettleCarts(List<MallCartVo> allCarts, List<Long> cartIds) {{
        List<MallCartVo> result = new ArrayList<>();
        for (MallCartVo cart : allCarts) {{
            boolean selected = cartIds != null && !cartIds.isEmpty()
                ? cartIds.contains(cart.getCartId()) : "1".equals(cart.getChecked());
            if (selected) result.add(cart);
        }}
        return result;
    }}

    private MallOrder requireOwnPendingOrder(Long userId, Long orderId) {{
        MallOrder order = orderMapper.selectMallOrderById(orderId);
        if (order == null || !userId.equals(order.getUserId())) throw new ServiceException(MSG_MISSING);
        if (!"0".equals(order.getStatus())) throw new ServiceException(MSG_PAY_ONLY);
        return order;
    }}

    private String generateOrderSn() {{
        return "MO" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 9999);
    }}
}}
''')


def fix_app_product_controller():
    path = os.path.join(ROOT, 'ruoyi-admin/src/main/java/com/ruoyi/web/controller/app/AppMallProductController.java')
    t = open(path, encoding='utf-8', errors='replace').read()
    t = re.sub(
        r'return error\("[^"]*"\);',
        'return error("' + J['PRODUCT_OFF'] + '");',
        t,
        count=1,
    )
    w('ruoyi-admin/src/main/java/com/ruoyi/web/controller/app/AppMallProductController.java', t)


def fix_mall_admin_controllers():
    base = 'ruoyi-admin/src/main/java/com/ruoyi/web/controller/mall'
    replacements = {
        'MallOrderController.java': [
            ('/ship/', '    @Log(title = "\u8ba2\u5355\u53d1\u8d27", businessType = BusinessType.UPDATE)'),
            ('/finish/', '    @Log(title = "\u8ba2\u5355\u5b8c\u6210", businessType = BusinessType.UPDATE)'),
            ('/cancel/', '    @Log(title = "\u8ba2\u5355\u53d6\u6d88", businessType = BusinessType.UPDATE)'),
            ('orderIds}', '    @Log(title = "\u8ba2\u5355\u5220\u9664", businessType = BusinessType.DELETE)'),
        ],
        'MallProductController.java': [
            ('@PostMapping', '    @Log(title = "\u5546\u54c1\u65b0\u589e", businessType = BusinessType.INSERT)'),
            ('@PutMapping', '    @Log(title = "\u5546\u54c1\u4fee\u6539", businessType = BusinessType.UPDATE)'),
            ('productIds}', '    @Log(title = "\u5546\u54c1\u5220\u9664", businessType = BusinessType.DELETE)'),
        ],
        'MallCategoryController.java': [
            ('@PostMapping', '    @Log(title = "\u5546\u54c1\u5206\u7c7b\u65b0\u589e", businessType = BusinessType.INSERT)'),
            ('@PutMapping', '    @Log(title = "\u5546\u54c1\u5206\u7c7b\u4fee\u6539", businessType = BusinessType.UPDATE)'),
            ('categoryIds}', '    @Log(title = "\u5546\u54c1\u5206\u7c7b\u5220\u9664", businessType = BusinessType.DELETE)'),
        ],
    }
    for fname, rules in replacements.items():
        path = os.path.join(ROOT, base, fname)
        lines = open(path, encoding='utf-8', errors='replace').read().split('\n')
        out = []
        ri = 0
        i = 0
        while i < len(lines):
            line = lines[i]
            if '@Log(title = "' in line and ri < len(rules):
                marker, new_log = rules[ri]
                j = i + 1
                while j < len(lines) and marker not in lines[j]:
                    j += 1
                out.append(new_log)
                ri += 1
            else:
                out.append(line)
            i += 1
        w(base + '/' + fname, '\n'.join(out))


def fix_social_callback():
    w('ruoyi-ui/src/views/social-callback.vue', '''<template>
  <div class="social-callback-page">
    <el-icon class="loading-icon" v-if="loading"><Loading /></el-icon>
    <p>{{ message }}</p>
  </div>
</template>

<script setup>
import { setToken } from '@/utils/auth'
import useUserStore from '@/store/modules/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const message = ref(''' + "'\u6b63\u5728\u767b\u5f55...'" + ''')

onMounted(async () => {
  const error = route.query.error
  const token = route.query.token
  const redirect = route.query.redirect || '/shop/home'
  if (error) {
    loading.value = false
    message.value = decodeURIComponent(error)
    setTimeout(() => router.replace('/login?redirect=' + encodeURIComponent(redirect)), 2000)
    return
  }
  if (!token) {
    loading.value = false
    message.value = ''' + "'\u767b\u5f55\u5931\u8d25\uff1a\u672a\u83b7\u53d6\u5230\u4ee4\u724c'" + '''
    setTimeout(() => router.replace('/login'), 2000)
    return
  }
  setToken(token)
  try {
    await userStore.getInfo()
    router.replace(redirect)
  } catch (e) {
    loading.value = false
    message.value = ''' + "'\u767b\u5f55\u5931\u8d25\uff0c\u8bf7\u91cd\u8bd5'" + '''
    setTimeout(() => router.replace('/login'), 2000)
  }
})
</script>

<style scoped>
.social-callback-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: #666;
}
.loading-icon { font-size: 40px; color: #ff6b35; animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
''')


def run_shop_scripts():
    order = [
        'write_shop_responsive.py',
        'write_shop_part2.py',
        'write_shop_auth.py',
        'patch_shop_user.py',
        'patch_shop_login_prompt.py',
    ]
    for name in order:
        p = os.path.join(SCRIPTS, name)
        print('run', name)
        subprocess.check_call([sys.executable, p], cwd=ROOT)


def scan_suspicious():
    bad = []
    patterns = [
        re.compile(r'[\ufffd]'),
        re.compile(r'\?\?\?\?'),
        re.compile(r'[\u00c0-\u00ff]{3,}'),  # latin extended runs (common mojibake)
        re.compile(r'_INFO|_NOW|_ORD|_ASK|_ED\b'),
    ]
    exts = {'.vue', '.js', '.java', '.sql'}
    skip_dirs = {'node_modules', 'target', '.git', 'dist'}
    for dirpath, dirnames, filenames in os.walk(ROOT):
        dirnames[:] = [d for d in dirnames if d not in skip_dirs]
        for fn in filenames:
            ext = os.path.splitext(fn)[1].lower()
            if ext not in exts:
                continue
            rel = os.path.relpath(os.path.join(dirpath, fn), ROOT)
            if 'ruoyi-ui' in rel and 'node_modules' in rel:
                continue
            if not any(x in rel for x in ('shop', 'mall', 'social', 'Mall', 'AppMall')):
                if ext == '.java' and 'mall' not in rel.lower() and 'social' not in rel.lower():
                    continue
            try:
                text = open(os.path.join(dirpath, fn), encoding='utf-8').read()
            except UnicodeDecodeError:
                bad.append((rel, 'invalid utf-8'))
                continue
            for pat in patterns:
                if pat.search(text):
                    bad.append((rel, pat.pattern))
                    break
    return bad


def gbk_to_utf8_file(rel):
    path = os.path.join(ROOT, rel.replace('/', os.sep))
    if not os.path.isfile(path):
        return
    raw = open(path, 'rb').read()
    try:
        raw.decode('utf-8')
        return
    except UnicodeDecodeError:
        pass
    for enc in ('gbk', 'gb2312', 'cp936'):
        try:
            text = raw.decode(enc)
            with open(path, 'w', encoding='utf-8', newline='\n') as f:
                f.write(text)
            print('gbk->utf8', rel, enc)
            return
        except UnicodeDecodeError:
            continue
    print('skip', rel)


def fix_binary_gbk_files():
    candidates = []
    for dirpath, dirnames, filenames in os.walk(ROOT):
        if any(x in dirpath for x in ('node_modules', 'target', '.git')):
            continue
        for fn in filenames:
            if not fn.endswith(('.java', '.sql', '.xml')):
                continue
            rel = os.path.relpath(os.path.join(dirpath, fn), ROOT)
            if 'ruoyi-mall' in rel or rel.startswith('sql' + os.sep):
                candidates.append(rel)
    for rel in candidates:
        gbk_to_utf8_file(rel)


def main():
    print('=== gbk -> utf8 (domain/sql) ===')
    fix_binary_gbk_files()
    print('=== fix Java services ===')
    # services overwrite messages; domain/sql converted above
    fix_mall_cart_service()
    fix_mall_order_service()
    fix_mall_app_order_service()
    fix_app_product_controller()
    fix_mall_admin_controllers()
    fix_social_callback()
    print('=== fix shop UI ===')
    run_shop_scripts()
    print('=== scan ===')
    issues = scan_suspicious()
    if issues:
        print('remaining suspicious files:')
        for rel, why in issues[:40]:
            print(' ', rel, '-', why)
    else:
        print('no suspicious patterns in mall/shop files')
    print('done')


if __name__ == '__main__':
    main()
