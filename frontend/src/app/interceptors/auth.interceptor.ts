import {
  HttpRequest,  
  HttpHandlerFn,
  HttpEvent,
  HttpInterceptorFn
} from '@angular/common/http';
import { Observable } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (
  request: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  const token = localStorage.getItem('token') || localStorage.getItem('temporaryToken');
  
  if (token) {
    const authRequest = request.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    return next(authRequest);
  }
  
  return next(request);
};
