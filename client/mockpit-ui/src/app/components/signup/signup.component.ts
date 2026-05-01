import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css'],
})
export class SignupComponent {
  loading = false;
  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    displayName: [''],
    password: ['', [Validators.required, Validators.minLength(12)]],
  });

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private toastr: ToastrService,
  ) {}

  submit(): void {
    if (this.form.invalid || this.loading) return;
    this.loading = true;
    const { email, password, displayName } = this.form.value;
    this.auth.signup(email!, password!, displayName || undefined).subscribe({
      next: () => {
        this.loading = false;
        this.toastr.success('Account created. You can now sign in.');
        this.router.navigateByUrl('/login');
      },
      error: err => {
        this.loading = false;
        this.toastr.error(err?.error?.message || 'Could not create account.', 'Sign up failed');
      },
    });
  }
}
