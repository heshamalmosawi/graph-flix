import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { ToastService } from '../shared/toast/toast.service';

function passwordValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.value;
  if (!password) {
    return null;
  }
  const hasLetter = /[a-zA-Z]/.test(password);
  const hasDigit = /[0-9]/.test(password);
  
  if (!hasLetter || !hasDigit) {
    return { passwordComplexity: true };
  }
  return null;
}

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './auth.html',
  styleUrl: './auth.scss'
})
export class AuthComponent {
  isLoginMode = true;
  authForm: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private toastService: ToastService
  ) {
    this.authForm = this.fb.group({
      name: [''],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8), passwordValidator]]
    });
  }

  onSwitchMode() {
    this.isLoginMode = !this.isLoginMode;
    
    // Reset form but keep values if needed, or clear all
    // Ideally clear validation for name if switching to login
    if (this.isLoginMode) {
      this.authForm.get('name')?.clearValidators();
      this.authForm.get('name')?.updateValueAndValidity();
    } else {
      this.authForm.get('name')?.setValidators([Validators.required]);
      this.authForm.get('name')?.updateValueAndValidity();
    }
  }

  onSubmit() {
    if (this.authForm.invalid) {
      return;
    }

    const { name, email, password } = this.authForm.value;
    this.isLoading = true;

    if (this.isLoginMode) {
      this.authService.login({ email, password }).subscribe({
        next: (res) => {
          this.isLoading = false;
          if (res.status === '2FA_REQUIRED') {
            this.toastService.info('Please complete two-factor authentication');
            this.router.navigate(['/auth/2fa/verify']);
          } else {
            this.toastService.success('Login successful!');
            this.router.navigate(['/']);
          }
        },
        error: (err) => {
          this.isLoading = false;
          this.toastService.error('Login failed. Please check your credentials.');
          console.error(err);
        }
      });
    } else {
      this.authService.register({ name, email, password }).subscribe({
        next: (res) => {
          this.isLoading = false;
          // After register, maybe auto-login or switch to login mode
          this.isLoginMode = true;
          this.toastService.success('Registration successful! Please login.');
        },
        error: (err) => {
          this.isLoading = false;
          this.toastService.error('Registration failed. Please try again.');
          console.error(err);
        }
      });
    }
  }
}
