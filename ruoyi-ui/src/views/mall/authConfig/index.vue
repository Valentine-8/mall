<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="邮箱验证码" name="email">
        <el-form ref="emailRef" :model="form" label-width="130px" style="max-width: 720px">
          <el-form-item label="启用邮箱登录">
            <el-switch v-model="emailEnabled" active-value="1" inactive-value="0" />
          </el-form-item>
          <el-form-item label="SMTP 主机">
            <el-input v-model="form.smtpHost" placeholder="smtp.qq.com" />
          </el-form-item>
          <el-form-item label="SMTP 端口">
            <el-input-number v-model="form.smtpPort" :min="1" :max="65535" />
          </el-form-item>
          <el-form-item label="SSL">
            <el-switch v-model="smtpSsl" active-value="1" inactive-value="0" />
          </el-form-item>
          <el-form-item label="SMTP 账号">
            <el-input v-model="form.smtpUser" placeholder="发信邮箱账号" />
          </el-form-item>
          <el-form-item label="SMTP 密码">
            <el-input v-model="form.smtpPassword" type="password" show-password placeholder="留空则不修改；QQ 邮箱填授权码" />
          </el-form-item>
          <el-form-item label="发件人名称">
            <el-input v-model="form.emailFromName" placeholder="国清商城" />
          </el-form-item>
          <el-form-item label="发件人地址">
            <el-input v-model="form.emailFromAddress" placeholder="默认同 SMTP 账号" />
          </el-form-item>
          <el-form-item label="邮件标题">
            <el-input v-model="form.emailSubject" />
          </el-form-item>
          <el-form-item label="邮件正文模板">
            <el-input v-model="form.emailBodyTemplate" type="textarea" :rows="3" placeholder="支持 {code} {minutes}" />
          </el-form-item>
          <el-form-item label="未配 SMTP 时模拟">
            <el-switch v-model="emailMock" active-value="1" inactive-value="0" />
            <span class="form-tip">开启后验证码写入后端日志，便于本地开发</span>
          </el-form-item>
          <el-divider content-position="left">通用</el-divider>
          <el-form-item label="验证码有效期">
            <el-input-number v-model="form.otpExpireMinutes" :min="1" :max="30" /> 分钟
          </el-form-item>
          <el-form-item label="发送间隔">
            <el-input-number v-model="form.otpSendInterval" :min="30" :max="300" /> 秒
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveConfig" v-hasPermi="['mall:auth:edit']">保存配置</el-button>
          </el-form-item>
          <el-alert type="info" show-icon :closable="false" title="QQ 邮箱示例：主机 smtp.qq.com，端口 465，SSL 开启，密码填授权码（非 QQ 密码）。" />
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="手机验证码" name="phone">
        <el-form :model="form" label-width="130px" style="max-width: 720px">
          <el-form-item label="启用手机登录">
            <el-switch v-model="phoneEnabled" active-value="1" inactive-value="0" />
            <span class="form-tip">正式环境需接入云短信后再开启</span>
          </el-form-item>
          <el-form-item label="未配短信时模拟">
            <el-switch v-model="smsMock" active-value="1" inactive-value="0" />
            <span class="form-tip">开启后验证码写入后端日志</span>
          </el-form-item>
          <el-form-item label="服务商">
            <el-select v-model="form.smsProvider" placeholder="暂未接入" clearable style="width: 100%">
              <el-option label="阿里云" value="aliyun" />
              <el-option label="腾讯云" value="tencent" />
            </el-select>
          </el-form-item>
          <el-form-item label="Access Key">
            <el-input v-model="form.smsAccessKey" />
          </el-form-item>
          <el-form-item label="Secret Key">
            <el-input v-model="form.smsSecretKey" type="password" show-password placeholder="留空则不修改" />
          </el-form-item>
          <el-form-item label="短信签名">
            <el-input v-model="form.smsSign" placeholder="审核通过的签名" />
          </el-form-item>
          <el-form-item label="模板 ID">
            <el-input v-model="form.smsTemplateId" placeholder="审核通过的模板 ID" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveConfig" v-hasPermi="['mall:auth:edit']">保存配置</el-button>
          </el-form-item>
          <el-alert type="warning" show-icon :closable="false" title="短信 SDK 尚未接入。可先开启「模拟」在日志中查看验证码；正式上线前配置阿里云/腾讯云并关闭模拟。" />
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup name="MallAuthConfig">
import { getCurrentInstance, onMounted, ref } from 'vue'
import { getMallAuthConfig, updateMallAuthConfig } from '@/api/mall/auth'

const { proxy } = getCurrentInstance()
const activeTab = ref('email')
const form = ref({})
const emailEnabled = ref('1')
const smtpSsl = ref('1')
const emailMock = ref('1')
const phoneEnabled = ref('0')
const smsMock = ref('1')

function loadConfig() {
  getMallAuthConfig().then(res => {
    form.value = res.data || {}
    emailEnabled.value = form.value.emailEnabled || '1'
    smtpSsl.value = form.value.smtpSsl || '1'
    emailMock.value = form.value.emailMock || '1'
    phoneEnabled.value = form.value.phoneEnabled || '0'
    smsMock.value = form.value.smsMock || '1'
  })
}

function saveConfig() {
  const payload = {
    ...form.value,
    emailEnabled: emailEnabled.value,
    smtpSsl: smtpSsl.value,
    emailMock: emailMock.value,
    phoneEnabled: phoneEnabled.value,
    smsMock: smsMock.value
  }
  updateMallAuthConfig(payload).then(() => {
    proxy.$modal.msgSuccess('保存成功')
    loadConfig()
  })
}

onMounted(() => loadConfig())
</script>

<style scoped>
.form-tip { margin-left: 12px; font-size: 12px; color: #909399; }
</style>
