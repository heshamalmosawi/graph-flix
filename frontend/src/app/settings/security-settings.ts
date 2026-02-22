import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../auth/auth.service';
import { ToastService } from '../shared/toast/toast.service';
import QRCode from 'qrcode';

@Component({
  selector: 'app-security-settings',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './security-settings.html',
  styleUrl: './security-settings.scss'
})
export class SecuritySettingsComponent implements OnInit {
  qrCodeUrl: string | null = null;
  secretKey: string | null = null;
  is2FAEnabled = false;
  isSetupMode = false;
  setupForm: FormGroup;
  disableForm: FormGroup;
  isSetupLoading = false;
  isEnableLoading = false;
  isDisableLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private toastService: ToastService
  ) {
    this.setupForm = this.fb.group({
      code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
    });

    this.disableForm = this.fb.group({
      code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
    });
  }

  ngOnInit() {
    this.check2FAStatus();
  }

  check2FAStatus() {
    this.is2FAEnabled = false;
  }

  onSetup2FA() {
    this.isSetupLoading = true;

    this.authService.setup2fa().subscribe({
      next: (response: any) => {
        this.isSetupLoading = false;
        this.secretKey = response.secret;
        this.generateQRCode(response.qrCode);
        this.isSetupMode = true;
        this.toastService.success('Scan the QR code with your authenticator app');
      },
      error: (err: any) => {
        this.isSetupLoading = false;
        this.toastService.error('Failed to setup 2FA. Please try again.');
        console.error(err);
      }
    });
  }

  async generateQRCode(qrData: string) {
    try {
      this.qrCodeUrl = await QRCode.toDataURL(qrData, {
        width: 200,
        margin: 2,
        color: {
          dark: '#6366f1',
          light: '#ffffff'
        }
      });
    } catch (err) {
      console.error('Error generating QR code:', err);
      this.toastService.error('Failed to generate QR code');
    }
  }

  onEnable2FA() {
    if (this.setupForm.invalid) {
      return;
    }

    const { code } = this.setupForm.value;
    this.isEnableLoading = true;

    this.authService.enable2fa(code).subscribe({
      next: (response: any) => {
        this.isEnableLoading = false;
        this.is2FAEnabled = true;
        this.isSetupMode = false;
        this.qrCodeUrl = null;
        this.secretKey = null;
        this.setupForm.reset();
        this.toastService.success('2FA enabled successfully');
      },
      error: (err: any) => {
        this.isEnableLoading = false;
        if (err.status === 400) {
          this.toastService.error(err.error?.error || 'Invalid verification code');
        } else {
          this.toastService.error('Failed to enable 2FA. Please try again.');
        }
        console.error(err);
      }
    });
  }

  onCancelSetup() {
    this.isSetupMode = false;
    this.qrCodeUrl = null;
    this.secretKey = null;
    this.setupForm.reset();
  }

  onDisable2FA() {
    if (this.disableForm.invalid) {
      return;
    }

    const { code } = this.disableForm.value;
    this.isDisableLoading = true;

    this.authService.disable2fa(code).subscribe({
      next: (response: any) => {
        this.isDisableLoading = false;
        this.is2FAEnabled = false;
        this.disableForm.reset();
        this.toastService.success('2FA disabled successfully');
      },
      error: (err: any) => {
        this.isDisableLoading = false;
        if (err.status === 400) {
          this.toastService.error(err.error?.error || 'Invalid verification code');
        } else {
          this.toastService.error('Failed to disable 2FA. Please try again.');
        }
        console.error(err);
      }
    });
  }

  copySecretKey() {
    if (this.secretKey) {
      navigator.clipboard.writeText(this.secretKey).then(() => {
        this.toastService.success('Secret key copied to clipboard');
      }).catch(() => {
        this.toastService.error('Failed to copy secret key');
      });
    }
  }
}
