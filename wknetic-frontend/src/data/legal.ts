// 服务条款和隐私政策内容
export interface LegalContent {
  title: string
  lastUpdated: string
  sections: {
    title: string
    content: string[]
  }[]
}

export const termsOfService = {
  en: {
    title: 'Terms of Service',
    lastUpdated: 'Last Updated: January 1, 2026',
    sections: [
      {
        title: '1. Acceptance of Terms',
        content: [
          'By accessing and using WkNetic ("the Service"), you accept and agree to be bound by the terms and provision of this agreement.',
          'If you do not agree to these Terms of Service, please do not use the Service.',
        ],
      },
      {
        title: '2. Description of Service',
        content: [
          'WkNetic provides a platform for Minecraft mod creators and users to discover, share, and download Minecraft modifications, plugins, and resource packs.',
          'We reserve the right to modify, suspend, or discontinue the Service at any time without notice.',
        ],
      },
      {
        title: '3. User Accounts',
        content: [
          'To access certain features of the Service, you must register for an account.',
          'You are responsible for maintaining the confidentiality of your account credentials.',
          'You agree to accept responsibility for all activities that occur under your account.',
          'You must notify us immediately of any unauthorized use of your account.',
        ],
      },
      {
        title: '4. User Content',
        content: [
          'Users may upload mods, plugins, resource packs, and other content ("User Content").',
          'You retain ownership of your User Content, but grant us a worldwide, non-exclusive license to use, reproduce, and distribute your content.',
          'You are solely responsible for your User Content and the consequences of posting it.',
          'You must not upload content that infringes on intellectual property rights, contains malware, or violates any laws.',
        ],
      },
      {
        title: '5. Prohibited Conduct',
        content: [
          'You agree not to: violate any laws or regulations; infringe on intellectual property rights; upload malicious code or viruses; harass or abuse other users; attempt to gain unauthorized access to the Service; scrape or copy content without permission.',
        ],
      },
      {
        title: '6. Intellectual Property',
        content: [
          'The Service and its original content (excluding User Content) are owned by WkNetic and are protected by copyright, trademark, and other intellectual property laws.',
          'You may not use our trademarks, logos, or branding without our express written permission.',
        ],
      },
      {
        title: '7. Termination',
        content: [
          'We reserve the right to suspend or terminate your account at any time, with or without cause or notice.',
          'Upon termination, your right to use the Service will immediately cease.',
        ],
      },
      {
        title: '8. Disclaimer of Warranties',
        content: [
          'The Service is provided "as is" without warranties of any kind, either express or implied.',
          'We do not guarantee that the Service will be uninterrupted, secure, or error-free.',
          'User Content is not reviewed by us, and we make no warranties about its safety, accuracy, or legality.',
        ],
      },
      {
        title: '9. Limitation of Liability',
        content: [
          'To the maximum extent permitted by law, WkNetic shall not be liable for any indirect, incidental, special, consequential, or punitive damages.',
          'Our total liability shall not exceed the amount you paid to us in the past 12 months.',
        ],
      },
      {
        title: '10. Changes to Terms',
        content: [
          'We reserve the right to modify these Terms at any time.',
          'Continued use of the Service after changes constitutes acceptance of the modified Terms.',
        ],
      },
      {
        title: '11. Contact Information',
        content: [
          'If you have any questions about these Terms, please contact us at: support@wknetic.com',
        ],
      },
    ],
  } as LegalContent,
  zh: {
    title: '服务条款',
    lastUpdated: '最后更新：2026年1月1日',
    sections: [
      {
        title: '1. 条款接受',
        content: [
          '通过访问和使用 WkNetic（"本服务"），您接受并同意受本协议条款和规定的约束。',
          '如果您不同意这些服务条款，请不要使用本服务。',
        ],
      },
      {
        title: '2. 服务说明',
        content: [
          'WkNetic 为 Minecraft 模组创作者和用户提供一个平台，用于发现、分享和下载 Minecraft 模组、插件和资源包。',
          '我们保留随时修改、暂停或终止服务的权利，恕不另行通知。',
        ],
      },
      {
        title: '3. 用户账户',
        content: [
          '要访问本服务的某些功能，您必须注册一个账户。',
          '您有责任维护账户凭据的机密性。',
          '您同意对您账户下发生的所有活动承担责任。',
          '如发现账户被未经授权使用，您必须立即通知我们。',
        ],
      },
      {
        title: '4. 用户内容',
        content: [
          '用户可以上传模组、插件、资源包和其他内容（"用户内容"）。',
          '您保留用户内容的所有权，但授予我们全球性、非独家的许可，以使用、复制和分发您的内容。',
          '您对您的用户内容及其发布的后果负全部责任。',
          '您不得上传侵犯知识产权、包含恶意软件或违反任何法律的内容。',
        ],
      },
      {
        title: '5. 禁止行为',
        content: [
          '您同意不：违反任何法律法规；侵犯知识产权；上传恶意代码或病毒；骚扰或辱骂其他用户；试图未经授权访问本服务；未经许可抓取或复制内容。',
        ],
      },
      {
        title: '6. 知识产权',
        content: [
          '本服务及其原创内容（不包括用户内容）由 WkNetic 拥有，受版权、商标和其他知识产权法保护。',
          '未经我们明确书面许可，您不得使用我们的商标、标识或品牌。',
        ],
      },
      {
        title: '7. 终止',
        content: [
          '我们保留随时暂停或终止您的账户的权利，无论是否有理由或通知。',
          '终止后，您使用本服务的权利将立即停止。',
        ],
      },
      {
        title: '8. 免责声明',
        content: [
          '本服务按"原样"提供，不提供任何明示或暗示的保证。',
          '我们不保证本服务将不间断、安全或无错误。',
          '用户内容未经我们审查，我们不对其安全性、准确性或合法性作出任何保证。',
        ],
      },
      {
        title: '9. 责任限制',
        content: [
          '在法律允许的最大范围内，WkNetic 不对任何间接、附带、特殊、后果性或惩罚性损害承担责任。',
          '我们的总责任不超过您在过去12个月内向我们支付的金额。',
        ],
      },
      {
        title: '10. 条款变更',
        content: [
          '我们保留随时修改这些条款的权利。',
          '在条款变更后继续使用本服务即表示接受修改后的条款。',
        ],
      },
      {
        title: '11. 联系信息',
        content: [
          '如果您对这些条款有任何疑问，请通过以下方式联系我们：support@wknetic.com',
        ],
      },
    ],
  } as LegalContent,
}

export const privacyPolicy = {
  en: {
    title: 'Privacy Policy',
    lastUpdated: 'Last Updated: January 1, 2026',
    sections: [
      {
        title: '1. Information We Collect',
        content: [
          'Account Information: When you create an account, we collect your username, email address, and password.',
          'Profile Information: You may optionally provide additional information such as a profile picture and bio.',
          'Content: We store the mods, plugins, and other content you upload to our platform.',
          'Usage Data: We collect information about how you use our Service, including access times, pages viewed, and interactions.',
          'Device Information: We may collect information about your device, including IP address, browser type, and operating system.',
        ],
      },
      {
        title: '2. How We Use Your Information',
        content: [
          'To provide, maintain, and improve our Service',
          'To create and manage your account',
          'To communicate with you about updates, security alerts, and support',
          'To analyze usage patterns and improve user experience',
          'To detect, prevent, and address technical issues and abuse',
          'To comply with legal obligations',
        ],
      },
      {
        title: '3. Information Sharing',
        content: [
          'We do not sell your personal information to third parties.',
          'We may share information with service providers who help us operate our platform (e.g., hosting providers, email services).',
          'We may disclose information if required by law or to protect our rights and safety.',
          'Public Content: Your username and uploaded content are publicly visible by default.',
        ],
      },
      {
        title: '4. Cookies and Tracking',
        content: [
          'We use cookies and similar tracking technologies to enhance your experience and analyze usage.',
          'You can control cookie settings through your browser preferences.',
          'Essential cookies are necessary for the Service to function and cannot be disabled.',
        ],
      },
      {
        title: '5. Data Security',
        content: [
          'We implement reasonable security measures to protect your information.',
          'However, no method of transmission over the internet is 100% secure.',
          'You are responsible for maintaining the security of your account credentials.',
        ],
      },
      {
        title: '6. Data Retention',
        content: [
          'We retain your information for as long as your account is active or as needed to provide services.',
          'You may request deletion of your account and data at any time.',
          'Some information may be retained for legal compliance or legitimate business purposes.',
        ],
      },
      {
        title: '7. Your Rights',
        content: [
          'Access: You can access and update your account information at any time.',
          'Deletion: You can request deletion of your account and personal data.',
          'Correction: You can correct inaccurate information.',
          'Objection: You can object to certain processing of your data.',
        ],
      },
      {
        title: '8. Children\'s Privacy',
        content: [
          'Our Service is not intended for users under 13 years of age.',
          'We do not knowingly collect information from children under 13.',
          'If we discover that we have collected information from a child under 13, we will delete it promptly.',
        ],
      },
      {
        title: '9. International Data Transfers',
        content: [
          'Your information may be transferred to and processed in countries other than your own.',
          'We take steps to ensure appropriate safeguards are in place for international transfers.',
        ],
      },
      {
        title: '10. Changes to This Policy',
        content: [
          'We may update this Privacy Policy from time to time.',
          'We will notify you of significant changes by email or through the Service.',
          'Continued use of the Service after changes constitutes acceptance of the updated policy.',
        ],
      },
      {
        title: '11. Contact Us',
        content: [
          'If you have questions about this Privacy Policy, please contact us at: privacy@wknetic.com',
        ],
      },
    ],
  } as LegalContent,
  zh: {
    title: '隐私政策',
    lastUpdated: '最后更新：2026年1月1日',
    sections: [
      {
        title: '1. 我们收集的信息',
        content: [
          '账户信息：当您创建账户时，我们收集您的用户名、电子邮件地址和密码。',
          '个人资料信息：您可以选择提供其他信息，如头像和简介。',
          '内容：我们存储您上传到我们平台的模组、插件和其他内容。',
          '使用数据：我们收集有关您如何使用我们服务的信息，包括访问时间、查看的页面和交互。',
          '设备信息：我们可能会收集有关您设备的信息，包括 IP 地址、浏览器类型和操作系统。',
        ],
      },
      {
        title: '2. 我们如何使用您的信息',
        content: [
          '提供、维护和改进我们的服务',
          '创建和管理您的账户',
          '就更新、安全警报和支持与您沟通',
          '分析使用模式并改善用户体验',
          '检测、预防和解决技术问题和滥用',
          '遵守法律义务',
        ],
      },
      {
        title: '3. 信息共享',
        content: [
          '我们不会将您的个人信息出售给第三方。',
          '我们可能会与帮助我们运营平台的服务提供商共享信息（例如托管提供商、电子邮件服务）。',
          '如果法律要求或为了保护我们的权利和安全，我们可能会披露信息。',
          '公开内容：您的用户名和上传的内容默认是公开可见的。',
        ],
      },
      {
        title: '4. Cookie 和跟踪',
        content: [
          '我们使用 Cookie 和类似的跟踪技术来增强您的体验并分析使用情况。',
          '您可以通过浏览器首选项控制 Cookie 设置。',
          '基本 Cookie 是服务正常运行所必需的，无法禁用。',
        ],
      },
      {
        title: '5. 数据安全',
        content: [
          '我们实施合理的安全措施来保护您的信息。',
          '但是，通过互联网传输的任何方法都不是 100% 安全的。',
          '您有责任维护账户凭据的安全。',
        ],
      },
      {
        title: '6. 数据保留',
        content: [
          '只要您的账户处于活动状态或需要提供服务，我们就会保留您的信息。',
          '您可以随时请求删除您的账户和数据。',
          '某些信息可能会因法律合规或合法业务目的而被保留。',
        ],
      },
      {
        title: '7. 您的权利',
        content: [
          '访问：您可以随时访问和更新您的账户信息。',
          '删除：您可以请求删除您的账户和个人数据。',
          '更正：您可以更正不准确的信息。',
          '反对：您可以反对对您数据的某些处理。',
        ],
      },
      {
        title: '8. 儿童隐私',
        content: [
          '我们的服务不适用于13岁以下的用户。',
          '我们不会故意收集13岁以下儿童的信息。',
          '如果我们发现收集了13岁以下儿童的信息，我们将立即删除。',
        ],
      },
      {
        title: '9. 国际数据传输',
        content: [
          '您的信息可能会被传输到您所在国家/地区以外的国家/地区并在那里处理。',
          '我们采取措施确保国际传输有适当的保障措施。',
        ],
      },
      {
        title: '10. 政策变更',
        content: [
          '我们可能会不时更新本隐私政策。',
          '我们将通过电子邮件或服务通知您重大变更。',
          '在变更后继续使用服务即表示接受更新后的政策。',
        ],
      },
      {
        title: '11. 联系我们',
        content: [
          '如果您对本隐私政策有任何疑问，请通过以下方式联系我们：privacy@wknetic.com',
        ],
      },
    ],
  } as LegalContent,
}
